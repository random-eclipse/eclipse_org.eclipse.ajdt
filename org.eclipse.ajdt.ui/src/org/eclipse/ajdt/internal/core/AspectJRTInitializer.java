/**********************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer - initial version 06-Nov-2003
...
**********************************************************************/
package org.eclipse.ajdt.internal.core;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathVariableInitializer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author colyer
 *
 */
public class AspectJRTInitializer extends ClasspathVariableInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ClasspathVariableInitializer#initialize(java.lang.String)
	 */
	public void initialize(String variable) {
		if (variable.equals("ASPECTJRT_LIB")) {
			// define it to point to aspectjrt.jar in ajde project.
			String ajrtPath =
				AspectJUIPlugin.
				getDefault().
				getAjdtProjectProperties().
				getAspectjrtClasspath();				
// in M4, this next line causes a StackOverflowError!!
//			JavaCore.removeClasspathVariable("ASPECTJRT_LIB",null);
			try {
				JavaCore.setClasspathVariable("ASPECTJRT_LIB",
						new Path(ajrtPath),null);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
