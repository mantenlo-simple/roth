/*
Copyright 2010 James M. Payne

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.roth.portal.model;

import java.io.Serializable;

public class Link implements Serializable {
	private static final long serialVersionUID = 3586417498344489073L;

	private String _title;
	private String _linkUri;
	private String _target;
	
	public String getTitle() { return _title; }
	public void setTitle(String title) { _title = title; }
	
	public String getLinkUri() { return _linkUri; }
	public void setLinkUri(String linkUri) { _linkUri = linkUri; }
	
	public String getTarget() { return _target; }
	public void setTarget(String target) { _target = target; }
}
