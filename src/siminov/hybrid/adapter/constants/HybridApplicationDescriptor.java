/** 
 * [SIMINOV FRAMEWORK]
 * Copyright [2013] [Siminov Software Solution|support@siminov.com]
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
 **/

package siminov.hybrid.adapter.constants;

public interface HybridApplicationDescriptor {

	public String APPLICATION_DESCRIPTOR = "ApplicationDescriptor";
	
	public String NAME = "name";
	public String DESCRIPTION = "description";
	
	public String VERSION = "version";
	
	public String LOAD_INITIALLY = "loadInitially";
	
	public String DATABASE_DESCRIPTORS = "Array";
	public String DATABASE_DESCRIPTOR_PATH = "databaseDescriptorPath";
	
	public String EVENT_NOTIFIERS = "Array";
	public String EVENT_NOTIFIER = "event";
	
}