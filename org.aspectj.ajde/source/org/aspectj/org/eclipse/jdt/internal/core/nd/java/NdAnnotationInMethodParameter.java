/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.internal.core.nd.java;

import org.aspectj.org.eclipse.jdt.internal.core.nd.Nd;
import org.aspectj.org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.aspectj.org.eclipse.jdt.internal.core.nd.field.StructDef;

public class NdAnnotationInMethodParameter extends NdAnnotation {
	public static final FieldManyToOne<NdMethodParameter> OWNER;

	@SuppressWarnings("hiding")
	public static final StructDef<NdAnnotationInMethodParameter> type;

	static {
		type = StructDef.create(NdAnnotationInMethodParameter.class, NdAnnotation.type);
		OWNER = FieldManyToOne.createOwner(type, NdMethodParameter.ANNOTATIONS);
		type.done();
	}

	public NdAnnotationInMethodParameter(Nd nd, long address) {
		super(nd, address);
	}

	public NdAnnotationInMethodParameter(Nd nd, NdMethodParameter owner) {
		super(nd);

		OWNER.put(getNd(), this.address, owner);
	}

}
