/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.ui.tests.visual;

import org.eclipse.ajdt.ui.tests.testutils.Utils;
import org.eclipse.contribution.xref.internal.ui.inplace.XReferenceInplaceDialog;
import org.eclipse.contribution.xref.ui.XReferenceUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

public class XReferenceInplaceDialogTest extends VisualTestCase {

	private IProject project;
	
	protected void setUp() throws Exception {	
		super.setUp();
		project = Utils.createPredefinedProject("bug102865");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		Utils.deleteProject(project);
	}
	
	public void testEscape() {
		IResource res = project.findMember("src/pack/A.aj");
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/A.aj file not found.");
		} 
		IFile ajFile = (IFile)res;

		// open Aspect.aj and select the pointcut
		final ITextEditor editorPart = (ITextEditor)Utils.openFileInAspectJEditor(ajFile, false);
		editorPart.setFocus();
		gotoLine(8);
		moveCursorRight(8);
		Utils.waitForJobsToComplete();
		
		// open inplace xref view
		XReferenceInplaceDialog dialog = openInplaceXRef(null);
		
		shutdownViewWithEscape(dialog);
		
		editorPart.close(false);
		Utils.waitForJobsToComplete();
		
	}
	
	public void testMoveAndResize() {
		IResource res = project.findMember("src/pack/A.aj");
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/A.aj file not found.");
		} 
		IFile ajFile = (IFile)res;

		// open Aspect.aj and select the pointcut
		final ITextEditor editorPart = (ITextEditor)Utils.openFileInAspectJEditor(ajFile, false);
		editorPart.setFocus();
		gotoLine(8);
		moveCursorRight(8);
		Utils.waitForJobsToComplete();
		
		// open inplace xref view
		final XReferenceInplaceDialog dialog = openInplaceXRef(null);

		// check that "remember size and location is checked"
		IDialogSettings settings = XReferenceUIPlugin.getDefault()
			.getDialogSettings().getSection("org.eclipse.contribution.internal.xref.QuickXRef");
		boolean disableRestoreLocation = settings.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_LOCATION);
		boolean disableRestoreSize = settings.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_SIZE);
		assertFalse("default setting should be to restore the location",disableRestoreLocation);
		assertFalse("default setting should be to restore the size",disableRestoreSize);
		
		// wait for the shell to be created
		new DisplayHelper() {

			protected boolean condition() {
				Shell s = dialog.getShell();
				boolean ret = (s != null);
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		
		final Shell shell = dialog.getShell();
		assertNotNull("the inplace xref view shell shouldn't be null",shell);
		final Rectangle r = shell.getBounds();
		
		moveShell(shell,r.x + 50,r.y + 50,r.width + 100,r.height + 100);

		// wait for the bounds to be set and it to move
		new DisplayHelper() {

			protected boolean condition() {
				Rectangle rect = shell.getBounds();
				boolean ret = (rect != null) && !(rect.equals(r));
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		
		Rectangle r1 = shell.getBounds();
		assertTrue("the inplace xref view should have changed it's height", r.height != r1.height);
		assertTrue("the inplace xref view should have changed it's width", r.width != r1.width);
		assertTrue("the inplace xref view should have changed it's x coordinate", r.x != r1.x);
		assertTrue("the inplace xref view should have changed it's y coordinate", r.y != r1.y);
				
		shutdownViewWithEscape(dialog);
		
		// open inplace xref view
		final XReferenceInplaceDialog dialog2 = openInplaceXRef(dialog);
		
		new DisplayHelper() {

			protected boolean condition() {
				Shell s = dialog2.getShell();
				boolean ret = s != null;
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		
		assertNotNull("the inplace xref dialog shell shouldn't be null",dialog2.getShell());
		
		Rectangle r2 = dialog2.getShell().getBounds();
		assertEquals("the inplace xref view should have remembered the changed height", r1.height, r2.height);
		assertEquals("the inplace xref view should have remembered the changed width", r1.width, r2.width);
		assertEquals("the inplace xref view should have remembered the changed x coordinate", r1.x, r2.x);
		assertEquals("the inplace xref view should have remembered the changed y coordinate", r1.y, r2.y);

		// set the "disable restore" settings to be true (which means
		// that next time the inplace xref view is brought up, the size
		// and position will be the defaults
		changeDisableRestoreSettings(settings,true);
		
		shutdownViewWithEscape(dialog2);
		
		editorPart.setFocus();
		gotoLine(8);
		moveCursorRight(8);
		Utils.waitForJobsToComplete();
		// open and get hold of the new inplace xref view
		final XReferenceInplaceDialog dialog3 = openInplaceXRef(dialog2);
		
		new DisplayHelper() {

			protected boolean condition() {
				Shell s = dialog3.getShell();
				boolean ret = s != null;
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		
		assertNotNull("the inplace xref dialog shell shouldn't be null",dialog3.getShell());

		
		new DisplayHelper() {

			protected boolean condition() {
				Rectangle r = dialog3.getDefaultBounds();
				boolean ret = (r != null);
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		
		Rectangle defaults = dialog3.getDefaultBounds();
		assertNotNull("the inplace views default bounds should not be null", defaults);
		
		Rectangle r3 = dialog3.getShell().getBounds();
		// for some bizarre reason on windows, 100 is added to the height and width,
		// and 50 is added to the x and y coordinates, on linux it is as expected,
		// therefore check equality for one or the other.
		assertTrue("the inplace xref view should have the default height", 
				r3.height == defaults.height || r3.height == defaults.height + 100 );
		assertTrue("the inplace xref view should have the default width", 
				r3.width == defaults.width || r3.width == defaults.width + 100 );
		assertTrue("he inplace xref view should have the default x coordinate", 
				r3.x == defaults.x || r3.x == defaults.x + 50 );
		assertTrue("the inplace xref view should have the default y coordinate", 
				r3.y == defaults.y || r3.y == defaults.y + 50 );

		// revert the disable restoring the location setting to it's default,
		// namely not to disable the restoring of the location - this
		// is just putting the settings back to their defaults.
		changeDisableRestoreSettings(settings,false);
	
		shutdownViewWithEscape(dialog3);
	}
	
	private void moveShell(Shell s, int xCoord, int yCoord, int width, int height) {
		Rectangle r1 = new Rectangle(xCoord,yCoord,width,height);
		s.setBounds(r1);
	}
	
	private void changeDisableRestoreSettings(IDialogSettings settings, boolean disable) {
		final boolean dis = disable;
		settings.put(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_LOCATION, dis);
		settings.put(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_SIZE, dis);
		
		new DisplayHelper() {

			protected boolean condition() {
				IDialogSettings s = XReferenceUIPlugin.getDefault()
					.getDialogSettings().getSection("org.eclipse.contribution.internal.xref.QuickXRef");
				boolean ret;
				if (dis) {
					ret = s.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_LOCATION)
						&& s.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_SIZE);
				} else {
					ret = !(s.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_LOCATION))
						&& !(s.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_SIZE));
				}
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);

		if (dis) {
			assertTrue("setting should be to disable restoring the location",settings.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_LOCATION));
			assertTrue("setting should be to disable restoring the size",settings.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_SIZE));		
		} else {
			assertFalse("setting should be to enable restoring the location",settings.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_LOCATION));
			assertFalse("setting should be to enable restoring the size",settings.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_SIZE));
		}

	}
	
	private void shutdownViewWithEscape(XReferenceInplaceDialog xrefDialog) {
		final XReferenceInplaceDialog dialog = xrefDialog;
		// press esc
		postCharacterKey(SWT.ESC);
		
		// wait a few secs
		new DisplayHelper() {

			protected boolean condition() {
				boolean ret = !dialog.isOpen();
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		
		assertFalse("xref inplace view should not be open",dialog.isOpen());

	}
	
	private XReferenceInplaceDialog openInplaceXRef(XReferenceInplaceDialog previousDialog) {
		final XReferenceInplaceDialog dialog = previousDialog;
		
		postKeyDown(SWT.CTRL);
		postKeyDown(SWT.ALT);
		postCharacterKey('x');
		postKeyUp(SWT.ALT);
		postKeyUp(SWT.CTRL);
		
		new DisplayHelper() {

			protected boolean condition() {
				XReferenceInplaceDialog i = XReferenceInplaceDialog.getInplaceDialog();
				boolean ret = (i != null) && !(i.equals(dialog));
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		
		final XReferenceInplaceDialog newDialog = XReferenceInplaceDialog.getInplaceDialog();
		assertNotNull("the inplace dialog shouldn't be null",newDialog);
		assertFalse("should have the new inplace dialog",newDialog.equals(dialog));
	
		new DisplayHelper() {

			protected boolean condition() {
				boolean ret = newDialog.isOpen();
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		
		assertTrue("xref inplace view should be open",newDialog.isOpen());
		
		return newDialog;
	}
	
}
