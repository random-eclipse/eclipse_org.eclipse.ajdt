/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.internal.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.ajdt.internal.builder.ProjectDependenciesUtils;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.test.utils.BlockingProgressMonitor;
import org.eclipse.ajdt.test.utils.JavaTestProject;
import org.eclipse.ajdt.test.utils.PluginTestProject;
import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.internal.ui.editor.plugin.ManifestEditor;

/**
 * @author hawkinsh
 *  
 */
public class AJDTUtilsTest extends TestCase {

	BlockingProgressMonitor monitor;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		monitor = new BlockingProgressMonitor();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAddAndRemoveAspectJNatureWithPluginProject() throws Exception {
		setUpPluginEnvironment();
		PluginTestProject testPluginProject = new PluginTestProject();
		ProjectDependenciesUtils.waitForJobsToComplete(testPluginProject.getProject());
		assertFalse("Plugin project shouldn't have AspectJ nature",
				testPluginProject.getProject().hasNature(
						AspectJUIPlugin.ID_NATURE));
		assertFalse("Plugin should not import AJDE plugin",
				hasDependencyOnAJDE(testPluginProject));
		AJDTUtils.addAspectJNature(testPluginProject.getProject());
		ProjectDependenciesUtils.waitForJobsToComplete(testPluginProject.getProject());
		assertTrue("Plugin project should now have AspectJ nature",
				testPluginProject.getProject().hasNature(
						AspectJUIPlugin.ID_NATURE));
		assertTrue("Plugin should now import AJDE plugin",
				hasDependencyOnAJDE(testPluginProject));
		AJDTUtils.removeAspectJNature(testPluginProject.getProject());
		ProjectDependenciesUtils.waitForJobsToComplete(testPluginProject.getProject());
		assertFalse("Plugin should not import AJDE plugin",
				hasDependencyOnAJDE(testPluginProject));
		assertFalse("Plugin project shouldn't have AspectJ nature",
				testPluginProject.getProject().hasNature(
						AspectJUIPlugin.ID_NATURE));
		testPluginProject.dispose();
		resetPluginEnvironment();
	}

	public void testAddAndRemoveAspectJNature() throws CoreException {
		AspectJPreferences.setAJDTPrefConfigDone(true);
		JavaTestProject testProject = new JavaTestProject("MyTestProject1");
		assertFalse("Java project should not have AspectJ Nature",
				testProject.getProject().hasNature(AspectJUIPlugin.ID_NATURE));
		assertFalse("Build path shouldn't contain aspectjrt.jar",
				hasAjrtOnBuildPath(testProject.getJavaProject()));
		AJDTUtils.addAspectJNature(testProject.getProject());
		assertTrue("Java project should now have AspectJ Nature",
				testProject.getProject().hasNature(AspectJUIPlugin.ID_NATURE));
		assertTrue("Build path should now contain aspectjrt.jar",
				hasAjrtOnBuildPath(testProject.getJavaProject()));
		AJDTUtils.removeAspectJNature(testProject.getProject());
		assertFalse("Java project should not have AspectJ Nature",
				testProject.getProject().hasNature(AspectJUIPlugin.ID_NATURE));
		assertFalse("Build path shouldn't contain aspectjrt.jar",
				hasAjrtOnBuildPath(testProject.getJavaProject()));
		testProject.dispose();
		AspectJPreferences.setAJDTPrefConfigDone(false);
	}

	/**
	 * This tests whether you get back the manifest editor for the project you
	 * require.
	 *  
	 */
	public void testGetPDEManifestEditor() throws Exception{
		setUpPluginEnvironment();
		PluginTestProject projectA1 = new PluginTestProject(
				"MyPluginProjectA1", "my.test.pluginA1", "ProjectA1Plugin",
				"projectA1.jar");
		PluginTestProject projectA2 = new PluginTestProject(
				"MyPluginProjectA2", "my.test.pluginA2", "ProjectA2Plugin",
				"projectA2.jar");
		PluginTestProject projectA3 = new PluginTestProject(
				"MyPluginProjectA3", "my.test.pluginA3", "ProjectA3Plugin",
				"projectA3.jar");
		PluginTestProject projectA4 = new PluginTestProject(
				"MyPluginProjectA4", "my.test.pluginA4", "ProjectA4Plugin",
				"projectA4.jar");
		assertTrue("projectA3 should have manifest editor for project A3",
				AJDTUtils.getAndPrepareToChangePDEModel(projectA3.getProject())
						.getPartName().equals(projectA3.getPluginID()));
		assertTrue("projectA1 should have manifest editor for project A1",
				AJDTUtils.getAndPrepareToChangePDEModel(projectA1.getProject())
						.getPartName().equals(projectA1.getPluginID()));
		assertTrue("projectA4 should have manifest editor for project A4",
				AJDTUtils.getAndPrepareToChangePDEModel(projectA4.getProject())
						.getPartName().equals(projectA4.getPluginID()));
		assertTrue("projectA2 should have manifest editor for project A2",
				AJDTUtils.getAndPrepareToChangePDEModel(projectA2.getProject())
						.getPartName().equals(projectA2.getPluginID()));
		projectA1.dispose();
		projectA2.dispose();
		projectA3.dispose();
		projectA4.dispose();
		resetPluginEnvironment();
	}

	public void testChangeProjectToClassDependencies() throws Exception{
		AspectJPreferences.setAJDTPrefConfigDone(true);
		JavaTestProject jtp1 = new JavaTestProject("JavaTestProject1");
		ProjectDependenciesUtils.waitForJobsToComplete(jtp1.getProject());
		JavaTestProject jtp2 = new JavaTestProject("JavaTestProject2");
		ProjectDependenciesUtils.waitForJobsToComplete(jtp2.getProject());
		// this ensures a src folder is created.
		jtp2.getSourceFolder();
		ProjectDependenciesUtils.waitForJobsToComplete(jtp2.getProject());			
		ProjectDependenciesUtils.addProjectDependency(jtp1.getJavaProject(),jtp2.getProject(),monitor);
		ProjectDependenciesUtils.waitForJobsToComplete(jtp1.getProject());
		ProjectDependenciesUtils.waitForJobsToComplete(jtp2.getProject());
		assertTrue("test project 1 has a project dependency on test project 2",
				checkDependencyType(jtp1.getJavaProject(),jtp2.getProject()).equals("project"));
		AJDTUtils.changeProjectDependencies(jtp2.getProject());
		ProjectDependenciesUtils.waitForJobsToComplete(jtp2.getProject());
		assertTrue("test project 1 has a class folder dependency on test project 2",
				checkDependencyType(jtp1.getJavaProject(),jtp2.getProject()).equals("classfolder"));			
		jtp1.dispose();
		jtp2.dispose();
		AspectJPreferences.setAJDTPrefConfigDone(false);
	}
	
	public void testAddAndRemoveAjrtToBuildPath() throws Exception {
		AspectJPreferences.setAJDTPrefConfigDone(true);
		IProject projectY = Utils.getPredefinedProject("project.java.Y", true);
		projectY.build(IncrementalProjectBuilder.FULL_BUILD, null);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		IJavaProject jY = JavaCore.create(projectY);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		
		assertFalse("project.java.Y should not have ajrt on build path",
				hasAjrtOnBuildPath(jY));
		AJDTUtils.addAjrtToBuildPath(projectY);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		
		assertTrue("project.java.Y should have ajrt on build path",
				hasAjrtOnBuildPath(jY));

		AJDTUtils.removeAjrtFromBuildPath(projectY);
		ProjectDependenciesUtils.waitForJobsToComplete(projectY);
		assertFalse("project.java.Y should not have ajrt on build path",
				hasAjrtOnBuildPath(jY));

		AspectJPreferences.setAJDTPrefConfigDone(false);
		
	}
	
	private String checkDependencyType(IJavaProject projectToHaveDependency, 
			IProject projectDependedOn) {
		try {
			IClasspathEntry[] cpEntry = projectToHaveDependency.getRawClasspath();
			for (int i = 0; i < cpEntry.length; i++) {
				IClasspathEntry entry = cpEntry[i];
				if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT
						&& entry.getPath().equals(projectDependedOn.getFullPath())) {
					return "project";
				} else if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY){
					List outputLocationPaths = AJDTUtils.getOutputLocationPaths(projectDependedOn);
					for (Iterator iterator = outputLocationPaths.iterator(); iterator.hasNext();) {
						IPath path = (IPath) iterator.next();
						if (entry.getPath().equals(path)) {
							return "classfolder";
						}
					}
				}	
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return "none";
	}
	
	private boolean hasAjrtOnBuildPath(IJavaProject javaProject) {
		try {
			IClasspathEntry[] originalCP = javaProject.getRawClasspath();
			for (int i = 0; i < originalCP.length; i++) {
				IPath path = originalCP[i].getPath();
				if (path.toOSString().endsWith("ASPECTJRT_LIB")
						|| path.toOSString().endsWith("aspectjrt.jar")) {
					return true;
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return false;
	}



	private void addImportToPDEModel(IPluginModel model, String importId)
			throws CoreException {

		IPluginImport importNode = model.getPluginFactory().createImport();
		importNode.setId(importId);
		model.getPluginBase().getImports();
		model.getPluginBase().add(importNode);

		IFile manifestFile = (IFile) model.getUnderlyingResource();
		//monitor.reset();
		manifestFile.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		//monitor.waitForCompletion();
	}

	private boolean hasDependencyOnAJDE(PluginTestProject testPluginProject) {
		ManifestEditor manEd = AJDTUtils.getAndPrepareToChangePDEModel(testPluginProject.getProject());
		if (manEd == null) {
			return false;
		}
		IPluginModel model = (IPluginModel) manEd.getAggregateModel();
		IPluginImport[] imports = model.getPluginBase().getImports();

		for (int i = 0; i < imports.length; i++) {
			IPluginImport importObj = imports[i];
			if (importObj.getId().equals(AspectJUIPlugin.RUNTIME_PLUGIN_ID)) {
				return true;
			}
		}
		return false;
	}

	private void setUpPluginEnvironment() throws CoreException {
		// set the project up so that when asked, the pde plugin
		// is added automatically and the preference configurations
		// have all been set up (therefore don't need user
		// interaction.
		AspectJPreferences.setAJDTPrefConfigDone(true);
		AspectJPreferences.setAskPDEAutoImport(false);
		AspectJPreferences.setDoPDEAutoImport(true);
		AspectJPreferences.setPDEAutoImportConfigDone(true);
	}

	private void resetPluginEnvironment() {
		IPreferenceStore ps = AspectJUIPlugin.getDefault().getPreferenceStore();
		ps.setToDefault(AspectJPreferences.PDE_AUTO_IMPORT_CONFIG_DONE);
		ps.setToDefault(AspectJPreferences.ASK_PDE_AUTO_IMPORT);
		ps.setToDefault(AspectJPreferences.DO_PDE_AUTO_IMPORT);
		ps.setToDefault(AspectJPreferences.AJDT_PREF_CONFIG_DONE);
	}
	

	public static class MyJobChangeListener implements IJobChangeListener {

		private List scheduledBuilds = new ArrayList();
		
		public void aboutToRun(IJobChangeEvent event) {
		}

		public void awake(IJobChangeEvent event) {
		}

		public void done(IJobChangeEvent event) {
			if (event.getJob().getPriority() == Job.BUILD ) {
				System.out.println(">> finished a build");
				scheduledBuilds.remove(event.getJob());
			}

		}

		public void running(IJobChangeEvent event) {
		}

		public void scheduled(IJobChangeEvent event) {
			if (event.getJob().getPriority() == Job.BUILD ) {
				System.out.println(">> scheduled a build");
				scheduledBuilds.add(event.getJob());
			}
		}

		public void sleeping(IJobChangeEvent event) {
		}
		
		public boolean buildsAreScheduled() {
			return !(scheduledBuilds.isEmpty());
		}
		
	}

}

