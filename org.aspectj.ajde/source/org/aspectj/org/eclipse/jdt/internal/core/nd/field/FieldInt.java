/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.internal.core.nd.field;

import org.aspectj.org.eclipse.jdt.internal.core.nd.Nd;
import org.aspectj.org.eclipse.jdt.internal.core.nd.db.Database;

/**
 * Declares a Nd field of type int. Can be used in place of  {@link Field}&lt{@link Integer}&gt in order to
 * avoid extra GC overhead.
 */
public class FieldInt implements IField {
	private int offset;

	public FieldInt() {
	}

	public int get(Nd nd, long address) {
		Database db = nd.getDB();
		return db.getInt(address + this.offset);
	}

	public void put(Nd nd, long address, int newValue) {
		nd.getDB().putInt(address + this.offset, newValue);
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset; 
	}

	@Override
	public int getRecordSize() {
		return Database.INT_SIZE;
	}
}
