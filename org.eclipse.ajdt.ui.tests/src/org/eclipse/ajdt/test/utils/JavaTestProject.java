/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - initial version
 *******************************************************************************/
package org.eclipse.ajdt.test.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;


/**
 * The base for this class was taken from the org.eclipse.contribution.junit.test
 * project (Gamma/Beck) and then edited from there.
 */
public class JavaTestProject {

	// projects don't delete reliably whatever we do, so to ensure that
	// every project within a test run is unique, we append a UUID to the
	// project name.
	private static int UNIQUE_ID = 1;
	
	private String name = "";
	private IProject project;
	private IJavaProject javaProject;
	private IPackageFragmentRoot sourceFolder;
	private IFolder binFolder;	
	
	//private static final String RT_JAR = "/opt/jdk1.4.2_04/jre/lib/rt.jar";
	//private static final String ASPECTJRT_JAR = "/home/colyer/aspectj1.2/lib/aspectjrt.jar";
	
	public JavaTestProject() throws CoreException {
		this("TestProject","bin",true);
	}
	
	public JavaTestProject(String pname) throws CoreException {
		this(pname,"bin",true);
	}
	
	public JavaTestProject(String pname, boolean createContent) throws CoreException {
		this(pname,"bin",createContent);
	}

	public JavaTestProject(String pname,String outputfoldername) throws CoreException {
		this(pname,outputfoldername,true);
	}
	
	public JavaTestProject(String pname,String outputfoldername,boolean createContent) throws CoreException {
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		name = pname + UNIQUE_ID++;
		project= root.getProject(name);
		project.create(null);
		project.open(null);
		javaProject = JavaCore.create(project);
		Utils.waitForJobsToComplete();
		setJavaNature();
		
		binFolder = createOutputFolder(outputfoldername);
		Utils.waitForJobsToComplete();		
		createOutputFolder(binFolder);
		Utils.waitForJobsToComplete();
		
		javaProject.setRawClasspath(new IClasspathEntry[0], null);
			
		if (createContent) {
			addSystemLibraries();
		}
	}

	private void setJavaNature() throws CoreException {
		IProjectDescription description= project.getDescription();
		description.setNatureIds(new String[] { JavaCore.NATURE_ID });
		project.setDescription(description, null);
	}
	public String getName() {
		return name;
	}
	
	public IProject getProject() {
		return project;
	}

	public IJavaProject getJavaProject() {
		return javaProject;
	}
	
	public void addJar(String plugin, String jar) throws MalformedURLException, IOException, JavaModelException {
		Path result= findFileInPlugin(plugin, jar);
		IClasspathEntry[] oldEntries= javaProject.getRawClasspath();
		IClasspathEntry[] newEntries= new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length]= JavaCore.newLibraryEntry(result, null, null);
		javaProject.setRawClasspath(newEntries, null);
	}

	public IPackageFragment createPackage(String name) throws CoreException {
		if (sourceFolder == null)
			sourceFolder= createSourceFolder();
		IPackageFragment ret = sourceFolder.createPackageFragment(name, false, null);
		return ret;
	}

	public IType createType(IPackageFragment pack, String cuName, String source) throws JavaModelException {
		StringBuffer buf= new StringBuffer();
		buf.append("package " + pack.getElementName() + ";\n");
		buf.append("\n");
		buf.append(source);
		ICompilationUnit cu= pack.createCompilationUnit(cuName, buf.toString(), false, null);
		//monitor.waitForCompletion();
		return cu.getTypes()[0];
	}

	public IPackageFragmentRoot getSourceFolder() throws CoreException {
		if (sourceFolder == null)
			sourceFolder= createSourceFolder();
		return sourceFolder;
	}
	
	public IFolder createFolder(IPackageFragmentRoot root, String name) throws JavaModelException, CoreException {
		IFolder folder = (IFolder) root.getCorrespondingResource();
		IFolder ret = folder.getFolder(name);
		if (!folder.exists()) {
			ret.create(true,true,null);
			//monitor.waitForCompletion();
		}
		return ret;
	}
	
	public IFile createFile(IFolder inFolder,String name, String content) throws CoreException {
		IFile file = inFolder.getFile(name);
		if (file.exists()) {
			file.delete(0,null);
		}
		ByteArrayInputStream source = new ByteArrayInputStream(content.getBytes());
		file.create(source,true,null);		
		return file;
	}
	
	public IFolder getOutputFolder() {
		return binFolder;
	}
	
	public synchronized void dispose() throws CoreException {
		Utils.deleteProject(project);
	}
	
//	public String run(String className) {
//		StringBuffer output = new StringBuffer();
//		try {
//			File binDir = new File(binFolder.getLocation().toOSString());
//			Process p = Runtime.getRuntime().exec("java -classpath " +
//							binDir.getPath() + ":" + ASPECTJRT_JAR + " " +  className,
//							null,new File(binFolder.getLocation().toOSString()));
//			InputStream is = p.getInputStream();
//			InputStreamReader isr = new InputStreamReader(is);
//			p.waitFor();
//			int c;
//			while ((c = isr.read()) != -1) {
//				output.append((char)c);
//			}
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		return output.toString();
//	}

	private synchronized IFolder createOutputFolder(String outputfoldername) throws CoreException {
		IFolder folder= project.getFolder(outputfoldername);
		if (!folder.exists()) {
			try {
				folder.create(true, true, null);
			} catch (ResourceException e) {
				// we don't care about this
			}
			//monitor.waitForCompletion();
		}
		return folder;
	}
	
	private void createOutputFolder(IFolder folder) throws JavaModelException {
		IPath outputLocation= folder.getFullPath();
		javaProject.setOutputLocation(outputLocation, null);
		//monitor.waitForCompletion();
	}

	private IPackageFragmentRoot createSourceFolder() throws CoreException {
		IFolder folder= project.getFolder("src");
		if (!folder.exists()) {
			folder.create(false, true, null);
			//monitor.waitForCompletion();
		}
		IPackageFragmentRoot root= javaProject.getPackageFragmentRoot(folder);

		IClasspathEntry[] oldEntries= javaProject.getRawClasspath();
		IClasspathEntry[] newEntries= new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length]= JavaCore.newSourceEntry(root.getPath());
		javaProject.setRawClasspath(newEntries, null);
		//monitor.waitForCompletion();
		return root;
	}

	private void addSystemLibraries() throws JavaModelException {
		IClasspathEntry[] oldEntries= javaProject.getRawClasspath();
		IClasspathEntry[] newEntries= new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length]= JavaRuntime.getDefaultJREContainerEntry();
//		newEntries[oldEntries.length]= JavaCore.newLibraryEntry(new Path(RT_JAR),null,null);
//		newEntries[oldEntries.length+1]= JavaCore.newLibraryEntry(new Path(ASPECTJRT_JAR),null,null);
		javaProject.setRawClasspath(newEntries, null);
		//monitor.waitForCompletion();

//		IClasspathEntry[] oldEntries= javaProject.getRawClasspath();
//		IClasspathEntry[] newEntries= new IClasspathEntry[oldEntries.length + 1];
//		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
//		newEntries[oldEntries.length]= JavaRuntime.getDefaultJREContainerEntry();
//		javaProject.setRawClasspath(newEntries, null);
	}

	private Path findFileInPlugin(String plugin, String file) throws MalformedURLException, IOException {
		org.osgi.framework.Bundle bundle = Platform.getBundle(plugin);
		URL pluginURL = bundle.getEntry("/");		
		URL jarURL= new URL(pluginURL, file);
		URL localJarURL= Platform.asLocalURL(jarURL);
		return new Path(localJarURL.getPath());
	}


//  public static class BlockingProgressMonitor implements IProgressMonitor {
//
//	private Boolean isDone = Boolean.FALSE;
//	
//	public boolean isDone() {
//		boolean ret = false;
//		synchronized(isDone) {
//		  ret = (isDone == Boolean.TRUE);
//		}
//		return ret;
//	}
//	
//	public void reset() {
//		synchronized(isDone) {
//		  isDone = Boolean.FALSE;
//		}
//	}
//	
//	public void waitForCompletion() {
//		while (!isDone()) {
//			try {
//				synchronized(this) { wait(500); }
//			} catch (InterruptedException intEx) {
//				// no-op
//			}
//		}
//	}
//	
//	public void beginTask(String name, int totalWork) {
//		if (name != null) System.out.println(name);
//		reset();
//	}
//
//	public void done() {
//		synchronized(isDone) {
//			isDone = Boolean.TRUE;
//		}
//		synchronized(this) {
//			notify();			
//		}
//	}
//
//	public void internalWorked(double work) {
//	}
//
//	public boolean isCanceled() {
//		return false;
//	}
//
//	public void setCanceled(boolean value) {
//	}
//
//	public void setTaskName(String name) {
//	}
//
//	public void subTask(String name) {
//	}
//
//	public void worked(int work) {
//	}
//  }	
//  

}
