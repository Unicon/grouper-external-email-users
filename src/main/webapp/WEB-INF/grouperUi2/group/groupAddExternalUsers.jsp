<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.groupContainer.guiGroup.breadcrumbs}
              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-group"></i> ${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}
                <br /><small>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.description)}</small></h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                  <form id="addExternalUser" class="form-horizontal">

                      <input type="hidden" name="groupId" value="${grouperRequestContainer.groupContainer.guiGroup.group.id}" />

                      <%--<c:if test="${not empty attributeDefinitions}">--%>
                      <div id="provTargetsMetadataId" >

                          <div class="control-group">
                              <label for="givenName" class="control-label">First Name:</label>

                              <div class="controls">
                                  <input type="text" id="givenName" name="givenName" value=""/>

                                  <span class="help-block">Required</span>
                              </div>
                          </div>
                          <div class="control-group">
                              <label for="surname" class="control-label">Last Name:</label>

                              <div class="controls">
                                  <input type="text" id="surname" name="surname" value=""/>

                                  <span class="help-block">Required</span>
                              </div>
                          </div>
                          <div class="control-group">
                              <label for="mail" class="control-label">E-mail:</label>

                              <div class="controls">
                                  <input type="email" id="mail" name="mail" value="" required />

                                  <span class="help-block">Required</span>
                              </div>
                          </div>

                      </div>

                      <div class="form-actions">
                          <a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2GroupExternalUsers.addSubmit', {formIds: 'addExternalUser'}); return false;">Update</a>
                          <a href="#" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');" >Cancel</a>
                      </div>

                  </form>

              </div>
            </div>
