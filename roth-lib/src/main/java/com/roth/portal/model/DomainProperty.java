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

import java.sql.SQLException;

import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.util.TableUtil;
import com.roth.portal.util.Portal;

@JdbcTable(name = "domain_property",
           primaryKeyColumns = {"domain_id", "property_name"})
public class DomainProperty extends AbstractProperty {
    private static final long serialVersionUID = 1L;

    private Long domainId;

    public Long getDomainId() { return domainId; }
    public void setDomainId(Long domainId) { this.domainId = domainId; }

    public static DomainProperty getDomainProperty(String userid, String propertyName) throws SQLException {
    	int dPos = userid.indexOf("@");
        String _domain = dPos < 0 ? "default" : userid.substring(dPos + 1);
        TableUtil t = new TableUtil("roth");
        String filter = t.applyParameters("domainId = (SELECT domain_id FROM domain WHERE domain_name = {1}) AND property_name = {2}", _domain, propertyName);
    	DomainProperty p = t.get(DomainProperty.class, filter);
    	if (p == null) {
    		p = new DomainProperty();
    		p.setDomainId(new Portal().getDomainId(_domain));
    		p.setPropertyName(propertyName);
    		p.setUpdatedBy(userid);
    	}
    	return p;
    }
    public static void setDomainProperty(DomainProperty p) throws SQLException { new TableUtil("roth").save(p); }
    public static void removeDomainProperty(DomainProperty p) throws SQLException { new TableUtil("roth").delete(p); }
}