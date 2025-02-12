/*
 * Copyright 2016-2017 Axioma srl.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.datastore.jpa.test.model.metamodel;

import java.io.Serializable;

import jakarta.persistence.Embeddable;

@Embeddable
public class MultiEmbeddedId implements Serializable {

	private static final long serialVersionUID = 1L;

	private long pk1;

	private String pk2;

	public long getPk1() {
		return pk1;
	}

	public void setPk1(long pk1) {
		this.pk1 = pk1;
	}

	public String getPk2() {
		return pk2;
	}

	public void setPk2(String pk2) {
		this.pk2 = pk2;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (pk1 ^ (pk1 >>> 32));
		result = prime * result + ((pk2 == null) ? 0 : pk2.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MultiEmbeddedId other = (MultiEmbeddedId) obj;
		if (pk1 != other.pk1)
			return false;
		if (pk2 == null) {
			if (other.pk2 != null)
				return false;
		} else if (!pk2.equals(other.pk2))
			return false;
		return true;
	}

}
