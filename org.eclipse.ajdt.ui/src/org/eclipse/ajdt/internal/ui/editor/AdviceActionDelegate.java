/**********************************************************************
Copyright (c) 2000, 2004 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
    Andy Clement, 1st Version, 7th October 2002
    Matt Chapman - add support for Go To Related Location entries
**********************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import java.io.File;

import org.eclipse.ajdt.internal.core.resources.AspectJImages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

public class AdviceActionDelegate extends AbstractRulerActionDelegate {


	IEditorPart editor;
	IVerticalRulerInfo rulerInfo;

	public AdviceActionDelegate() {
		editor    = null;
		rulerInfo = null;
	}



	/**
	 * @see IEditorActionDelegate#setActiveEditor(bIAction, IEditorPart)
	 */
	public void setActiveEditor(IAction callerAction,IEditorPart targetEditor) {
		// We only care about compilation unit and class file editors
		if (targetEditor != null) {
			String id = targetEditor.getSite().getId();

			if (!id.equals(JavaUI.ID_CU_EDITOR) && !id.equals(JavaUI.ID_CF_EDITOR)
				&& !id.equals(AspectJEditor.ASPECTJ_EDITOR_ID)) // The AspectJ editor
				targetEditor = null;
		}
		editor = targetEditor; // Remember the editor
		super.setActiveEditor(callerAction, targetEditor);
	}



	/**
	 * @see AbstractRulerActionDelegate#createAction()
	 */
	protected IAction createAction(ITextEditor editor,IVerticalRulerInfo rulerInfo) {
		this.rulerInfo = rulerInfo;
		return null;
	}



    /**
     * Called to see if this action delegate wants to influence the menu before it
     * is displayed - in the case of AJDT we have to check if there is an advice
     * marker in affect on the line in which the user has right clicked.  If there
     * is then we add an 'Advised By' line to the context submenu that
     * will appear.  By going through the submenu and selecting advice, we force
     * the editor to jump to a particular file and location - selecting the
     * advice that is in effect.
     */
	public void menuAboutToShow(IMenuManager manager) {

		try {
			// Work out which file is currently being edited
			IFileEditorInput ifep =	(IFileEditorInput) this.editor.getEditorInput();
			IFile ifile = ifep.getFile();
			
			// Which line was right clicked in the ruler?
			int linenumber = rulerInfo.getLineOfLastMouseButtonActivity();

			// Go through the advice markers attached to this file, if any have a line number that
			// matches the line clicked by the user, create the submenu (if it has not already
			// been created) and add a new submenu entry for the advice.  The data that is
			// stored with the submenu entry gives the run() method of the inner class the
			// ability to create a jump marker such that it can jump to the location where
			// the advice is defined.
			IMarker markers[] = ifile.findMarkers(AspectJUIPlugin.ADVICE_MARKER, true, 2);		    
			MenuManager adviceSubmenu = null;
		    boolean adviceSubmenuInitialized = false;
			if (markers != null && markers.length != 0) {
				
				Integer clickedLine = new Integer(linenumber+1);
				
				for (int j = 0; j < markers.length; j++) {
					IMarker m = markers[j];
					
					if (m.getAttribute(IMarker.LINE_NUMBER).equals(clickedLine)) {
						String textLabel = ((String)m.getAttribute(IMarker.MESSAGE));//.substring(8);
						// substring(8) skips the 'Advice: ' bit on the front.
						
						// Build a new action for our menu.  Set the text label and remember the
						// marker (an advice marker) in effect on this line, so that if the run
						// method of the action is driven, it can correctly jump to the right
						// location in the aspect.
						AJDTMenuAction ama = new AJDTMenuAction(textLabel,m);
						
						// Initialize the submenu if we haven't done it already.
						if (!adviceSubmenuInitialized) {
							adviceSubmenu = new MenuManager(
							  AspectJUIPlugin.getResourceString("EditorRulerContextMenu.adviceInAffect"));
							manager.add(new Separator());
							manager.add(adviceSubmenu);			
							adviceSubmenuInitialized = true; 
						}
							
					    // Add our new action to the submenu
						adviceSubmenu.add(ama);
					}
				}
			}

			// Go through the ITD markers attached to this file, if any have a line number that
			// matches the line clicked by the user, create the submenu (if it has not already
			// been created) and add a new submenu entry for the advice.  The data that is
			// stored with the submenu entry gives the run() method of the inner class the
			// ability to create a jump marker such that it can jump to the location where
			// the advice is defined.
			IMarker decMarkers[] = ifile.findMarkers(AspectJUIPlugin.DECLARATION_MARKER, true, 2);			
			MenuManager declarationSubmenu = null;
		    boolean declarationSubmenuInitialized = false;
			if (decMarkers != null && decMarkers.length != 0) {
				
				Integer clickedLine = new Integer(linenumber+1);
				
				for (int j = 0; j < decMarkers.length; j++) {
					IMarker m = decMarkers[j];
					
					if (m.getAttribute(IMarker.LINE_NUMBER).equals(clickedLine)) {
						String textLabel = ((String)m.getAttribute(IMarker.MESSAGE));//.substring(8);
						// substring(8) skips the 'Advice: ' bit on the front.
						
						// Build a new action for our menu.  Set the text label and remember the
						// marker (an advice marker) in effect on this line, so that if the run
						// method of the action is driven, it can correctly jump to the right
						// location in the aspect.
						AJDTMenuAction ama = new AJDTMenuAction(textLabel,m);
						
						// Initialize the submenu if we haven't done it already.
						if (!declarationSubmenuInitialized) {
							declarationSubmenu = new MenuManager(
							  AspectJUIPlugin.getResourceString("EditorRulerContextMenu.aspectDeclarations"));
							if(!adviceSubmenuInitialized) {
								manager.add(new Separator());
							}
							manager.add(declarationSubmenu);			
							declarationSubmenuInitialized = true; 
						}
							
					    // Add our new action to the submenu
						declarationSubmenu.add(ama);
					}
				}
			}
			
			// Go through the problem markers attached to this file, if any have a line number that
			// matches the line clicked by the user, create the submenu (if it has not already
			// been created) and add a new submenu entry for the advice.  The data that is
			// stored with the submenu entry gives the run() method of the inner class the
			// ability to create a jump marker such that it can jump to the location where
			// the advice is defined.
			IMarker probMarkers[] = ifile.findMarkers(IMarker.PROBLEM, true, 2);
            MenuManager problemSubmenu = null;
            boolean problemSubmenuInitialized = false;
            if (probMarkers != null && probMarkers.length != 0) {
                Integer clickedLine = new Integer(linenumber + 1);
                for (int j = 0; j < probMarkers.length; j++) {
                    IMarker m = probMarkers[j];
                    if (m.getAttribute(IMarker.LINE_NUMBER).equals(clickedLine)) {
                        int relCount = 0;
                        String loc = (String) m
                                .getAttribute(AspectJUIPlugin.RELATED_LOCATIONS_ATTRIBUTE_PREFIX
                                        + (relCount++));
                        if (loc != null) {
                            // Build a new action for our menu for each extra
                            // source location
                            while (loc != null) {
                                // decode the source location
                                String[] s = loc.split(":::");
                                String resName = s[0].substring(s[0]
                                        .lastIndexOf(File.separator) + 1);
                                String textLabel = AspectJUIPlugin
                                        .getFormattedResourceString(
                                                "EditorRulerContextMenu.relatedLocation.message",
                                                new String[] { resName, s[1] });
                                RelatedLocationMenuAction ama = new RelatedLocationMenuAction(
                                        textLabel, loc);
                                // Initialize the submenu if we haven't done it
                                // already.
                                if (!problemSubmenuInitialized) {
                                    problemSubmenu = new MenuManager(
                                            AspectJUIPlugin
                                                    .getResourceString("EditorRulerContextMenu.relatedLocations"));
                                    if (!(adviceSubmenuInitialized || declarationSubmenuInitialized)) {
                                        manager.add(new Separator());
                                    }
                                    manager.add(problemSubmenu);
                                    problemSubmenuInitialized = true;
                                }

                                // Add our new action to the submenu
                                problemSubmenu.add(ama);

                                loc = (String) m
                                        .getAttribute(AspectJUIPlugin.RELATED_LOCATIONS_ATTRIBUTE_PREFIX
                                                + (relCount++));
                            }
                        }
                    }
                }
            }
        } catch (CoreException ce) {
            AspectJUIPlugin
                    .getDefault()
                    .getErrorHandler()
                    .handleError(
                            "Exception whilst extending ruler context menu with advice items",
                            ce);
        }
    }
	
	
	
	/**
	 * Inner classes that represent an entry on the submenu for "Advised By >" 
	 * or "Aspect Declarations >" or "Go To Related Location >"
	 * - each AJDTMenuAction is a piece of advice or an ITD in affect on the current line.
	 * When each AJDTMenuAction is created, it is given a name (the advice in affect)
	 * and a marker.  This is the advice marker attached to the line.  Both advice markers
	 * and ITD markers are like normal markers but have an extra attribute: 
	 * AspectJPlugin.SOURCE_LOCATION_ATTRIBUTE
	 * This attribute has the format FFFF:::NNNN:::NNNN:::NNNN
	 * - The FFFF is the file which contains the source of the advice or ITD in affect
	 * - The other three NNNN fields are integers indicating (in order) the
	 *   start line number of the advice in that file, the end line number of the
	 *   advice in that file and the column number for the advice.
	 * 
	 * I had to code it this way because you can't set arbitrary object values for
	 * attributes.  Using the value of this attribute, the run() method for the
	 * action can create a jump marker that points to the real advice definition
	 * and jump to it.
	 */
	abstract class BaseAJDTMenuAction extends Action {
        BaseAJDTMenuAction(String s) {
            super(s);
        }

        abstract String getJumpLocation();

        public void run() {

            // Fetch the real advice marker from the marker that is attached to
            // affected sites.
            try {
                // Take jumpLocation apart. It is initially: FFFF:::NNNN:::NNNN:::NNNN
                String[] s = getJumpLocation().split(":::");                               
                final String filepath = s[0];
                final String linenumber = s[1];
                // System.err.println("FilePath=" + filepath);
                // System.err.println("linenum=" + linenumber);

                final IResource ir = AspectJUIPlugin.getDefault()
                        .getAjdtProjectProperties().findResource(filepath);

                IMarker jumpMarker = null;

                if ((ir != null) && (ir.exists())) {
                    try {
                        jumpMarker = ir.createMarker(IMarker.TEXT);
                        /*
                         * GOTCHA: If setting LINE_NUMBER for a marker, you
                         * *have* to call the version of setAttribute that takes
                         * an int and not the version that takes a string (even
                         * if your line number is in a string) - it won't give
                         * you an error but will *not* be interpreted correctly.
                         */
                        jumpMarker.setAttribute(IMarker.LINE_NUMBER,
                                new Integer(linenumber).intValue());

                    } catch (CoreException ce) {
                        AspectJUIPlugin
                                .getDefault()
                                .getErrorHandler()
                                .handleError(
                                        "Unable to build jump marker in AdviceMenuAction run method",
                                        ce);
                    }

                    try {
                        IDE.openEditor(AspectJUIPlugin.getDefault()
                                .getActiveWorkbenchWindow().getActivePage(),
                                jumpMarker, true);
                    } catch (Exception e) {
                        AspectJUIPlugin
                                .getDefault()
                                .getErrorHandler()
                                .handleError(
                                        "Exception whilst asking editor to jump to advice location",
                                        e);

                    }
                } else {
                    report("Could not find resource.");
                }
            } catch (IndexOutOfBoundsException ioobe) {
                AspectJUIPlugin
                        .getDefault()
                        .getErrorHandler()
                        .handleError(
                                "Problem parsing marker jump location (probably!)",
                                ioobe);
            }
        }
    }
	
	class RelatedLocationMenuAction extends BaseAJDTMenuAction {
	    private String jumpLocation;
	    
	    RelatedLocationMenuAction(String s, String jumpLocation) {
	        super(s);
	        this.jumpLocation = jumpLocation;
	        setImageDescriptor(JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_CUNIT));
	    }
	    
	       String getJumpLocation() {
	           return jumpLocation;
	       }
	}
	
	class AJDTMenuAction extends BaseAJDTMenuAction {

        // Advice marker, the AspectJPlugin.SOURCE_LOCATION_ATTRIBUTE attribute is the only
        // attribute used.
        IMarker adviceMarker;

        /**
         * Set the name, remember the marker and set the image !
         */
        AJDTMenuAction(String s, IMarker marker) {
            super(s);
            String runtimeTest = AspectJUIPlugin
                    .getResourceString("AspectJEditor.runtimetest");
            boolean hasRuntimeTest = false;
            if (s.endsWith(runtimeTest)) {
                hasRuntimeTest = true;
            }
            if (s.indexOf(".before(") != -1) {
                if (hasRuntimeTest) {
                    setImageDescriptor(AspectJImages.DYNAMIC_BEFORE_ADVICE
                            .getImageDescriptor());
                } else {
                    setImageDescriptor(AspectJImages.BEFORE_ADVICE
                            .getImageDescriptor());
                }
            } else if (s.indexOf(".after(") != -1
                    || s.indexOf(".afterReturning(") != -1
                    || s.indexOf(".afterThrowing(") != -1) {
                if (hasRuntimeTest) {
                    setImageDescriptor(AspectJImages.DYNAMIC_AFTER_ADVICE
                            .getImageDescriptor());
                } else {
                    setImageDescriptor(AspectJImages.AFTER_ADVICE
                            .getImageDescriptor());
                }
            } else if (s.indexOf(".around(") != -1) {
                if (hasRuntimeTest) {
                    setImageDescriptor(AspectJImages.DYNAMIC_AROUND_ADVICE
                            .getImageDescriptor());
                } else {
                    setImageDescriptor(AspectJImages.AROUND_ADVICE
                            .getImageDescriptor());
                }
            } else {
                setImageDescriptor(AspectJImages.ITD.getImageDescriptor());
            }
            adviceMarker = marker;
        }

        String getJumpLocation() {
            try {
                return (String) adviceMarker
                        .getAttribute(AspectJUIPlugin.SOURCE_LOCATION_ATTRIBUTE);
            } catch (CoreException ce) {
                AspectJUIPlugin
                        .getDefault()
                        .getErrorHandler()
                        .handleError(
                                "Exception whilst executing AdviceMenuAction run method",
                                ce);
                return null;
            }
        }
    }
	
	protected void report(final String message) {
		JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				IEditorStatusLine fStatusLine = (IEditorStatusLine) editor.getAdapter(IEditorStatusLine.class);
				if (fStatusLine != null) {
					fStatusLine.setMessage(true, message, null);
				}
				if (message != null
						&& JDIDebugUIPlugin.getActiveWorkbenchShell() != null) {
					Display.getCurrent().beep();
				}
			}
		});
	}

}
