/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.lang;

import java.util.EventObject;

/**
 * An event used to propagate meta class updates
 * @author <a href="mailto:blackdrag@gmx.org">Jochen Theodorou</a>
 */
public class MetaClassRegistryChangeEvent extends EventObject {
	private Class clazz;
	private MetaClass metaClass;
	
	public MetaClassRegistryChangeEvent(Object source, Class clazz, MetaClass metaClass ) {
		super(source);
		this.clazz = clazz;
		this.metaClass = metaClass;
	}
	
	public Class getClassToUpdate() {
		return clazz;
	}
	
	public MetaClass getNewMetaClass() {
		return metaClass;
	}

}
