/**********************************************************************
Copyright (c) 2003, 2004 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Matt Chapman - initial version
Ian McGrath - Adapted for the properties page
**********************************************************************/

package org.eclipse.ajdt.internal.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.preferences.PreferencePageSupport;
import org.eclipse.jdt.internal.ui.preferences.PreferencesMessages;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.jdt.internal.ui.util.TabFolderLayout;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ajdt.internal.ui.preferences.AJCompilerPreferencePage;

/**
* Used to operate the AspectJ compiler properties page that appear when an aspectJ project is right
* clicked and the properties option selected, found under the AspectJ Compiler tab.
*
*/
public class CompilerPropertyPage extends PropertyPage implements SelectionListener {

	public final String COMPILER_PB_INVALID_ABSOLUTE_TYPE_NAME = AspectJPreferences.OPTION_ReportInvalidAbsoluteTypeName;
	public final String COMPILER_PB_INVALID_WILDCARD_TYPE_NAME = AspectJPreferences.OPTION_ReportInvalidWildcardTypeName;
	public final String COMPILER_PB_UNRESOLVABLE_MEMBER = AspectJPreferences.OPTION_ReportUnresolvableMember;
	public final String COMPILER_PB_TYPE_NOT_EXPOSED_TO_WEAVER = AspectJPreferences.OPTION_ReportTypeNotExposedToWeaver;
	public final String COMPILER_PB_SHADOW_NOT_IN_STRUCTURE = AspectJPreferences.OPTION_ReportShadowNotInStructure;
	public final String COMPILER_PB_UNMATCHED_SUPERTYPE_IN_CALL = AspectJPreferences.OPTION_ReportUnmatchedSuperTypeInCall;
	public final String COMPILER_PB_CANNOT_IMPLEMENT_LAZY_TJP = AspectJPreferences.OPTION_ReportCannotImplementLazyTJP;
	public final String COMPILER_PB_NEED_SERIAL_VERSION_UID = AspectJPreferences.OPTION_ReportNeedSerialVersionUIDField;
	public final String COMPILER_PB_INCOMPATIBLE_SERIAL_VERSION = AspectJPreferences.OPTION_ReportIncompatibleSerialVersion;
	public final String COMPILER_PB_NO_INTERFACE_CTOR_JOINPOINT = AspectJPreferences.OPTION_ReportNoInterfaceCtorJoinpoint;
	
	public final String COMPILER_NO_WEAVE = AspectJPreferences.OPTION_NoWeave;
	public final String COMPILER_SERIALIZABLE_ASPECTS = AspectJPreferences.OPTION_XSerializableAspects;
	public final String COMPILER_LAZY_TJP = AspectJPreferences.OPTION_XLazyThisJoinPoint;
	public final String COMPILER_NO_ADVICE_INLINE = AspectJPreferences.OPTION_XNoInline;
	public final String COMPILER_REWEAVABLE = AspectJPreferences.OPTION_XReweavable;
	public final String COMPILER_REWEAVABLE_COMPRESS = AspectJPreferences.OPTION_XReweavableCompress;

	private final String PREF_AJ_INVALID_ABSOLUTE_TYPE_NAME = COMPILER_PB_INVALID_ABSOLUTE_TYPE_NAME;
	private final String PREF_AJ_SHADOW_NOT_IN_STRUCTURE = COMPILER_PB_SHADOW_NOT_IN_STRUCTURE;
	private final String PREF_AJ_CANNOT_IMPLEMENT_LAZY_TJP = COMPILER_PB_CANNOT_IMPLEMENT_LAZY_TJP;
	private final String PREF_AJ_INVALID_WILDCARD_TYPE_NAME = COMPILER_PB_INVALID_WILDCARD_TYPE_NAME;
	private final String PREF_AJ_TYPE_NOT_EXPOSED_TO_WEAVER = COMPILER_PB_TYPE_NOT_EXPOSED_TO_WEAVER;
	private final String PREF_AJ_UNRESOLVABLE_MEMBER = COMPILER_PB_UNRESOLVABLE_MEMBER;
	private final String PREF_AJ_UNMATCHED_SUPER_TYPE_IN_CALL = COMPILER_PB_UNMATCHED_SUPERTYPE_IN_CALL;
	private final String PREF_AJ_INCOMPATIBLE_SERIAL_VERSION = COMPILER_PB_INCOMPATIBLE_SERIAL_VERSION;
	private final String PREF_AJ_NEED_SERIAL_VERSION_UID_FIELD = COMPILER_PB_NEED_SERIAL_VERSION_UID;
	private final String PREF_AJ_NO_INTERFACE_CTOR_JOINPOINT = COMPILER_PB_NO_INTERFACE_CTOR_JOINPOINT;

	private final String PREF_ENABLE_NO_WEAVE = COMPILER_NO_WEAVE;
	private final String PREF_ENABLE_SERIALIZABLE_ASPECTS = COMPILER_SERIALIZABLE_ASPECTS;
	private final String PREF_ENABLE_LAZY_TJP = COMPILER_LAZY_TJP;
	private final String PREF_ENABLE_NO_INLINE = COMPILER_NO_ADVICE_INLINE;
	private final String PREF_ENABLE_REWEAVABLE = COMPILER_REWEAVABLE;
	private final String PREF_ENABLE_REWEAVABLE_COMPRESS = COMPILER_REWEAVABLE_COMPRESS;

	private static final String ERROR = JavaCore.ERROR;
	private static final String WARNING = JavaCore.WARNING;
	private static final String IGNORE = JavaCore.IGNORE;

	private static String ENABLED = JavaCore.ENABLED;
	private static String DISABLED = JavaCore.DISABLED;

	private Button noweaveButton, lazytjpButton, noinlineButton, reweaveButton, reweaveCompressButton; 
	
	private IProject thisProject;
	private boolean initialised = false; //if the default properties settings have been entered into the store
	private int switches; // counts the number of switches between use project settings and use workspace settings
	
	protected List fComboBoxes;
	protected List fCheckBoxes;
	private SelectionButtonDialogField fUseWorkspaceSettings;
	private SelectionButtonDialogField fChangeWorkspaceSettings;
	private SelectionButtonDialogField fUseProjectSettings;
	private TabFolder folder;

	public CompilerPropertyPage() {
		super();
		fCheckBoxes = new ArrayList();
		fComboBoxes = new ArrayList();
		switches = 0; 
		
		IDialogFieldListener listener= new IDialogFieldListener() {
			public void dialogFieldChanged(DialogField field) {
				doDialogFieldChanged(field);
			}
		};
		
		fUseWorkspaceSettings = new SelectionButtonDialogField(SWT.RADIO);
		fUseWorkspaceSettings.setDialogFieldListener(listener);
		fUseWorkspaceSettings.setLabelText(PreferencesMessages.getString("CompilerPropertyPage.useworkspacesettings.label")); //$NON-NLS-1$

		fChangeWorkspaceSettings= new SelectionButtonDialogField(SWT.PUSH);
		fChangeWorkspaceSettings.setLabelText(PreferencesMessages.getString("CompilerPropertyPage.useworkspacesettings.change")); //$NON-NLS-1$
		fChangeWorkspaceSettings.setDialogFieldListener(listener);
	
		fUseWorkspaceSettings.attachDialogField(fChangeWorkspaceSettings);

		fUseProjectSettings= new SelectionButtonDialogField(SWT.RADIO);
		fUseProjectSettings.setDialogFieldListener(listener);
		fUseProjectSettings.setLabelText(PreferencesMessages.getString("CompilerPropertyPage.useprojectsettings.label")); //$NON-NLS-1$
	}
	
	protected static class ControlData {
		private String fKey;
		private String[] fValues;

		public ControlData(String key, String[] values) {
			fKey = key;
			fValues = values;
		}

		public String getKey() {
			return fKey;
		}

		public String getValue(boolean selection) {
			int index = selection ? 0 : 1;
			return fValues[index];
		}

		public String getValue(int index) {
			return fValues[index];
		}

		public int getSelection(String value) {
			for (int i = 0; i < fValues.length; i++) {
				if (value.equals(fValues[i])) {
					return i;
				}
			}
			return 0;
		}
	}

	/**
	 * The default values used when the plugin is first installed or when
	 * "restore defaults" is clicked.
	 */
	public void initDefaults(IPreferenceStore store) {
		
		store.setDefault(thisProject + PREF_AJ_INVALID_ABSOLUTE_TYPE_NAME, WARNING);
		store.setDefault(thisProject + PREF_AJ_SHADOW_NOT_IN_STRUCTURE, IGNORE);
		store.setDefault(thisProject + PREF_AJ_CANNOT_IMPLEMENT_LAZY_TJP, WARNING);
		store.setDefault(thisProject + PREF_AJ_INVALID_WILDCARD_TYPE_NAME, IGNORE);
		store.setDefault(thisProject + PREF_AJ_TYPE_NOT_EXPOSED_TO_WEAVER, WARNING);
		store.setDefault(thisProject + PREF_AJ_UNRESOLVABLE_MEMBER, WARNING);
		store.setDefault(thisProject + PREF_AJ_UNMATCHED_SUPER_TYPE_IN_CALL, WARNING);
		store.setDefault(thisProject + PREF_AJ_INCOMPATIBLE_SERIAL_VERSION, IGNORE);
		store.setDefault(thisProject + PREF_AJ_NEED_SERIAL_VERSION_UID_FIELD, IGNORE);
		store.setDefault(thisProject + PREF_AJ_NO_INTERFACE_CTOR_JOINPOINT, WARNING);
		
		store.setDefault(thisProject + PREF_ENABLE_NO_WEAVE, false);
		store.setDefault(thisProject + PREF_ENABLE_SERIALIZABLE_ASPECTS, false);
		store.setDefault(thisProject + PREF_ENABLE_LAZY_TJP, false);
		store.setDefault(thisProject + PREF_ENABLE_NO_INLINE, false);
		store.setDefault(thisProject + PREF_ENABLE_REWEAVABLE, false);
		store.setDefault(thisProject + PREF_ENABLE_REWEAVABLE_COMPRESS, false);
		
		store.setDefault(thisProject + "useProjectSettings", false);
		initialised = true;
	}

	/**
	 * from IWorkbenchPreferencePage
	 */
	public void init(IWorkbench workbench) {
	}

	/**
	 * from IWorkbenchPreferencePage
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
	}

	/*
	 * Generates the GUI and initialises variables
	 */
	protected Control createContents(Composite parent) {

		thisProject = (IProject) getElement();	
		if(!initialised) {
			initDefaults(getPreferenceStore());
		}
		
		//Composite is the project-workspace settings selection part of the gui at the top of the page
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		composite.setLayout(layout);
		
		fUseWorkspaceSettings.doFillIntoGrid(composite, 1);
		LayoutUtil.setHorizontalGrabbing(fUseWorkspaceSettings.getSelectionButton(null));
		
		fChangeWorkspaceSettings.doFillIntoGrid(composite, 1);
		
		fUseProjectSettings.doFillIntoGrid(composite, 2);
		
		GridData data= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL );
		data.horizontalSpan= 2;
		
		//folder holds the tabed selection chart on the lower two thirds of the properties page
		folder = new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(data);

		Composite aspectjComposite = createMessagesTabContent(folder);
		TabItem item = new TabItem(folder, SWT.NONE);
		item
				.setText(AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.aj_messages.tabtitle")); //$NON-NLS-1$
		item.setControl(aspectjComposite);

		aspectjComposite = createAdvancedTabContent(folder);
		item = new TabItem(folder, SWT.NONE);
		item
				.setText(AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.aj_advanced.tabtitle")); //$NON-NLS-1$
		item.setControl(aspectjComposite);
		
		Dialog.applyDialogFont(composite);
		IPreferenceStore store = getPreferenceStore();
		if(store.getBoolean(thisProject + "useProjectSettings"))
			fUseProjectSettings.setSelection(true);
		else
			fUseWorkspaceSettings.setSelection(true);
		updateEnableState();
		return composite;
	}
	
	/*
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	private Composite createMessagesTabContent(Composite folder) {
		String[] errorWarningIgnore = new String[]{ERROR, WARNING, IGNORE};

		String[] errorWarningIgnoreLabels = new String[]{
				AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.error"), //$NON-NLS-1$
				AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.warning"), //$NON-NLS-1$
				AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.ignore") //$NON-NLS-1$
		};

		int nColumns = 3;

		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;

		Composite composite = new Composite(folder, SWT.NULL);
		composite.setLayout(layout);

		Label description = new Label(composite, SWT.WRAP);
		description
				.setText(AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.aj_messages.description")); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = nColumns;
		//gd.widthHint= fPixelConverter.convertWidthInCharsToPixels(50);
		description.setLayoutData(gd);

		String label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_invalid_absolute_type_name.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_INVALID_ABSOLUTE_TYPE_NAME,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_invalid_wildcard_type_name.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_INVALID_WILDCARD_TYPE_NAME,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_unresolvable_member.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_UNRESOLVABLE_MEMBER,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_type_not_exposed_to_weaver.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_TYPE_NOT_EXPOSED_TO_WEAVER,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_shadow_not_in_structure.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_SHADOW_NOT_IN_STRUCTURE,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_unmatched_super_type_in_call.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_UNMATCHED_SUPER_TYPE_IN_CALL,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_cannot_implement_lazy_tjp.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_CANNOT_IMPLEMENT_LAZY_TJP,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_need_serial_version_uid_field.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_NEED_SERIAL_VERSION_UID_FIELD,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_incompatible_serial_version.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_INCOMPATIBLE_SERIAL_VERSION,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.aj_no_interface_ctor_joinpoint.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_AJ_NO_INTERFACE_CTOR_JOINPOINT,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);
		return composite;
	}
	
	/**
	 * Generates the gui for the Advanced tab
	 */
	private Composite createAdvancedTabContent(Composite folder) {
		String[] enableDisableValues = new String[]{ENABLED, DISABLED};
		
		CheckBoxListener checkBoxListener = new CheckBoxListener();
		
		int nColumns = 3;

		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;

		Composite composite = new Composite(folder, SWT.NULL);
		composite.setLayout(layout);

		Label description = new Label(composite, SWT.WRAP);
		description
				.setText(AspectJUIPlugin
						.getResourceString("CompilerConfigurationBlock.aj_advanced.description")); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = nColumns;
		//gd.widthHint= fPixelConverter.convertWidthInCharsToPixels(50);
		description.setLayoutData(gd);

		String label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_no_weave.label"); //$NON-NLS-1$
		noweaveButton = addCheckBox(composite, label, PREF_ENABLE_NO_WEAVE, enableDisableValues, 0);
		noweaveButton.addSelectionListener(checkBoxListener);
		
		label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_x_serializable_aspects.label"); //$NON-NLS-1$
		addCheckBox(composite, label, PREF_ENABLE_SERIALIZABLE_ASPECTS,enableDisableValues, 0);

		label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_x_lazy_tjp.label"); //$NON-NLS-1$
		lazytjpButton = addCheckBox(composite, label, PREF_ENABLE_LAZY_TJP,enableDisableValues, 0);

		label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_x_no_inline.label"); //$NON-NLS-1$
		noinlineButton = addCheckBox(composite, label, PREF_ENABLE_NO_INLINE,enableDisableValues, 0);

		label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_x_reweavable.label"); //$NON-NLS-1$
		reweaveButton = addCheckBox(composite, label, PREF_ENABLE_REWEAVABLE,enableDisableValues, 0);
		reweaveButton.addSelectionListener(checkBoxListener);

		label = AspectJUIPlugin.getResourceString("CompilerConfigurationBlock.aj_x_reweavable_compress.label"); //$NON-NLS-1$
		reweaveCompressButton = addCheckBox(composite, label, PREF_ENABLE_REWEAVABLE_COMPRESS,enableDisableValues, 0);
		reweaveCompressButton.addSelectionListener(checkBoxListener);

		checkNoWeaveSelection();
		
		return composite;
	}

	/**
	 * Get the preference store for AspectJ mode
	 */
	protected IPreferenceStore doGetPreferenceStore() {
		return AspectJUIPlugin.getDefault().getPreferenceStore();
	}
	
	/*
	 * (non-Javadoc) Method declared on PreferencePage
	 */
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();

		boolean lintChanges = false;
		for (int i = fComboBoxes.size() - 1; i >= 0; i--) {
			Combo curr = (Combo) fComboBoxes.get(i);
			ControlData data = (ControlData) curr.getData();
			String value = data.getValue(curr.getSelectionIndex());
			if (!value.equals(store.getString(thisProject.toString() + data.getKey()))) {
				lintChanges = true;
				store.setValue(thisProject + data.getKey(), value);
			}
		}

		boolean advancedChanges = false;
		for (int i = fCheckBoxes.size() - 1; i >= 0; i--) {
			Button curr = (Button) fCheckBoxes.get(i);
			ControlData data = (ControlData) curr.getData();
			boolean value = curr.getSelection();
			if (value != store.getBoolean(thisProject.toString() + data.getKey())) {
				advancedChanges = true;
				store.setValue(thisProject.toString() + data.getKey(), value);
			}
		}
		
		boolean projectWorkspaceChanges = false;
		if(store.getBoolean(thisProject + "useProjectSettings") !=  useProjectSettings()) {
			projectWorkspaceChanges = true;
			store.setValue(thisProject + "useProjectSettings" , useProjectSettings());
		}
		
		AspectJUIPlugin.getDefault().savePluginPreferences();

		if (lintChanges || advancedChanges || projectWorkspaceChanges || ((switches > 1) && useProjectSettings())) {
			boolean doBuild = false;
			String[] strings = getProjectBuildDialogStrings();
			if (strings != null) {
				MessageDialog dialog = new MessageDialog(getShell(),
						strings[0], null, strings[1], MessageDialog.QUESTION,
						new String[]{IDialogConstants.YES_LABEL,
								IDialogConstants.NO_LABEL,
								IDialogConstants.CANCEL_LABEL}, 2);
				int res = dialog.open();
				if ((res == 0)) {
					doBuild = true;
				} else if (res != 1) {
					return false; // cancel pressed
				}
			}
			if (doBuild) {
				doProjectBuild();
			}
		}

		return true;
	}

	/**
	 * Get the preference store for AspectJ mode
	 */
	protected String[] getProjectBuildDialogStrings() {
		String title = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.needsbuild.title"); //$NON-NLS-1$
		String message = AspectJUIPlugin
				.getResourceString("CompilerConfigurationBlock.needsprojectbuild.message"); //$NON-NLS-1$
		return new String[]{title, message};
	}

	protected void doProjectBuild() { 
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		try {
			dialog.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException {
					monitor.beginTask("", 2); //$NON-NLS-1$
					try {
						monitor
								.setTaskName(AspectJUIPlugin
										.getResourceString("OptionsConfigurationBlock.buildproject.taskname")); //$NON-NLS-1$
						thisProject.build(
								IncrementalProjectBuilder.FULL_BUILD,
								new SubProgressMonitor(monitor, 2));
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			});
		} catch (InterruptedException e) {
			// cancelled by user
		} catch (InvocationTargetException e) {
			String message = AspectJUIPlugin
					.getResourceString("OptionsConfigurationBlock.builderror.message"); //$NON-NLS-1$
			AspectJUIPlugin.getDefault().getErrorHandler().handleError(message, e);
		}
	}

	/*
	 * (non-Javadoc) Method declared on PreferencePage
	 */
	protected void performDefaults() {
		super.performDefaults();

		fUseWorkspaceSettings.setSelection(true);
		fUseProjectSettings.setSelection(false);
		
		for (int i = fComboBoxes.size() - 1; i >= 0; i--) {
			Combo curr = (Combo) fComboBoxes.get(i);
			ControlData data = (ControlData) curr.getData();
			String defaultValue = getPreferenceStore().getDefaultString(
					data.getKey());
			curr.select(data.getSelection(defaultValue));
		}
		for (int i = fCheckBoxes.size() - 1; i >= 0; i--) {
			Button curr = (Button) fCheckBoxes.get(i);
			// must enable the button as it may have been disabled
			// if -XnoWeave was previously selected

			if(useProjectSettings())
				curr.setEnabled(true);
			ControlData data = (ControlData) curr.getData();
			String defaultValue = getPreferenceStore().getDefaultString(
					data.getKey());
			curr.setSelection(data.getSelection(defaultValue) == 1);
		}
	}

	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}
	
	protected Button addCheckBox(Composite parent, String label, String key,
			String[] values, int indent) {
		ControlData data = new ControlData(key, values);

		int idx = label.indexOf("-");
		String optionname = label.substring(0,idx);
		String optiondesc = label.substring(idx+1);
		optiondesc=optiondesc.trim();
		
		GridData gd = new GridData(GridData.FILL_VERTICAL);//HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 3;
		gd.horizontalIndent = indent;
		

		Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(optionname);
		checkBox.setData(data);
		checkBox.setLayoutData(gd);
		Label l = new Label(parent,SWT.WRAP);
		l.setText(optiondesc);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalIndent = 20;
		l.setLayoutData(gridData);
		createLabel(parent,"");//filler
		

		boolean currValue = getPreferenceStore().getBoolean(thisProject.toString() + key);
		checkBox.setSelection(currValue);

		fCheckBoxes.add(checkBox);

		return checkBox;
	}

	protected void addComboBox(Composite parent, String label, String key,
			String[] values, String[] valueLabels, int indent) {
		ControlData data = new ControlData(key, values);

		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent = indent;

		Label labelControl = new Label(parent, SWT.LEFT | SWT.WRAP);
		labelControl.setText(label);
		labelControl.setLayoutData(gd);

		Combo comboBox = new Combo(parent, SWT.READ_ONLY);
		comboBox.setItems(valueLabels);
		comboBox.setData(data);
		comboBox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		Label placeHolder = new Label(parent, SWT.NONE);
		placeHolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		String currValue = getPreferenceStore().getString(thisProject.toString() + key);
		if ((currValue != null) && (currValue.length() > 0)) {
			comboBox.select(data.getSelection(currValue));
		}

		fComboBoxes.add(comboBox);
	}

	/**
	 * Class which listens for selections of the advanced options
	 * -XnoWeave, -Xreweavable and -Xreweavable:compress and updates
	 * the remaining buttons accordingly (to make it less confusing
	 * for the user)
	 */	
	private class CheckBoxListener implements SelectionListener {

		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(noweaveButton)) {
				boolean buttonSelected = noweaveButton.getSelection();
				if (buttonSelected) {
					lazytjpButton.setSelection(false);
					noinlineButton.setSelection(false);
					reweaveButton.setSelection(false);
					reweaveCompressButton.setSelection(false);
				}				
				lazytjpButton.setEnabled(!buttonSelected);
				noinlineButton.setEnabled(!buttonSelected);
				reweaveButton.setEnabled(!buttonSelected);
				reweaveCompressButton.setEnabled(!buttonSelected);
			} else if (e.getSource().equals(reweaveButton)) {
				boolean buttonSelected = reweaveButton.getSelection();
				if (buttonSelected) {
					reweaveCompressButton.setSelection(false);
				}
			} else if (e.getSource().equals(reweaveCompressButton)) {
				boolean buttonSelected = reweaveCompressButton.getSelection();
				if (buttonSelected) {
					reweaveButton.setSelection(false);
				}
			}
		}
		public void widgetDefaultSelected(SelectionEvent e) {
		}		
	}
	
	/**
	 * When the advanced tab is initialized, check whether the
	 * user last chose the -XnoWeave option, if so, disable all
	 * other options.
	 */	
	private void checkNoWeaveSelection() {
		boolean buttonSelected = noweaveButton.getSelection();
		if (buttonSelected) {
			lazytjpButton.setEnabled(!buttonSelected);
			noinlineButton.setEnabled(!buttonSelected);
			reweaveButton.setEnabled(!buttonSelected);
			reweaveCompressButton.setEnabled(!buttonSelected);
		}						
	}
	
	public void widgetDefaultSelected(SelectionEvent se) {
		widgetSelected(se);
	}
	
	public void widgetSelected(SelectionEvent se) {
	}
	
	/**
	 * Listens to various buttons and can load the workspace preference page in a seperate window
	 * and determines if the enabled status of the GUI's button and checkboxes need refreshing
	 */
	private void doDialogFieldChanged(DialogField field) {
		if (field == fChangeWorkspaceSettings) {
			String id= "org.eclipse.ajdt.ui.preferences.AJCompilerPreferencePage"; //$NON-NLS-1$
			AJCompilerPreferencePage page= new AJCompilerPreferencePage();
			PreferencePageSupport.showPreferencePage(getShell(), id, page);
		} else {
			switches++;
			updateEnableState();
		}
	}

	/**
	 * Enables and disables the appropriate buttons
	 */	
	private void updateEnableState() {
		if (useProjectSettings()) {
			for(int i = 0; i< fComboBoxes.size(); i++) {
				Combo box = (Combo)fComboBoxes.get(i);
				box.setEnabled(true);
			}
			for(int i = 0; i< fCheckBoxes.size(); i++) {
				Button checkBox = (Button)fCheckBoxes.get(i);
				checkBox.setEnabled(true);
			}
			boolean buttonSelected = noweaveButton.getSelection();
			if (buttonSelected) {
				lazytjpButton.setSelection(false);
				noinlineButton.setSelection(false);
				reweaveButton.setSelection(false);
				reweaveCompressButton.setSelection(false);
			}				
			lazytjpButton.setEnabled(!buttonSelected);
			noinlineButton.setEnabled(!buttonSelected);
			reweaveButton.setEnabled(!buttonSelected);
			reweaveCompressButton.setEnabled(!buttonSelected);
		}
		else {
			for(int i = 0; i< fComboBoxes.size(); i++) {
				Combo box = (Combo)fComboBoxes.get(i);
				box.setEnabled(false);
			}
			for(int i = 0; i< fCheckBoxes.size(); i++) {
				Button checkBox = (Button)fCheckBoxes.get(i);
				checkBox.setEnabled(false);
			}	 
		}
	}
		
	/**
	 * Checks the status of the project settings button, returns true if selected
	 */
	private boolean useProjectSettings() {
		return fUseProjectSettings.isSelected();
	}
}
