/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.buildconfigurator;

/**
 * To notify listeners when ACTIVE build configuration changed somehow
 * 
 * @author Luzius Meisser
 */
public interface IBuildConfigurationChangedListener {
	public void buildConfigurationChanged(ProjectBuildConfigurator pbc);
}
