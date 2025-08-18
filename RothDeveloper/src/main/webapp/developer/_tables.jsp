<%--
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
 --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>
<%@taglib uri="roth" prefix="r"%>
<%@taglib uri="roth-functions" prefix="rf"%>

<r:jspSecurity rolesAllowed="SystemAdmin,Developer"/>

<c:forEach var="table" items="${requestScope.tables}" varStatus="stat">
    <div class="unselectable"
         style="display: block; cursor: pointer;" 
         ondblclick="if (isIE) window.event.cancelBuble = true; 
                     else event.stopPropagation();"
         onmouseover="if (!this.className.contains('dsiTblSel')) this.style.background = 'whitesmoke';" 
         onmouseout="this.style.background = '';"
         onclick="_$v('tableParams', 'jndiname=' + _$v('jndiname') + '&tableId=${table.tableId}&schema=${param['schema']}&tableName=${table.tableName}');
                  if (!selDsiTbl(this))
                      return;
                  tableId = '${table.tableId}';
                  tableName = '${table.tableName}';
                  //rtmu(getChild(_$('objtabs'), '0.0'));
                  Roth.tabset.setSelected(_$('objtabs'), 'columns');
                  _$('indexes').innerHTML = '';
                  _$('foreignkeys').innerHTML = '';
                  _$('triggers').innerHTML = '';
                  Roth.ajax.htmlAction('columns', '/RothDeveloper/Developer/getDatasourceInfo', 'infoType=C&jndiname=' + _$v('jndiname') + '&schema=${param['schema']}&tableId=${table.tableId}');">
        <span>&nbsp;&nbsp;&nbsp;</span>
        <span style="color: darkgray;">${stat.index eq fn:length(requestScope.tables) - 1 ? '&boxur;&nbsp;' : '&boxvr;&nbsp;'}</span>
        ${table.tableName}
    </div>
    <r:break/>
</c:forEach>
