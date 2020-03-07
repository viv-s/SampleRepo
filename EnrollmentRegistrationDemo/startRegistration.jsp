<!DOCTYPE html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<c:set var="jsFile" value="register.js" />
<c:set var="page" value="startRegistration" />
<%@ include file="header.jsp" %>
<%@ include file="contactUs.jsp" %>

	<div class="container">
		<div class="row">
			<!-- left navigation pane -->
			<%@ include file="navigation.jsp" %>	
			
			<!-- main form -->
			<main id="registerContainer" class="col-md-9 col-sm-8 col-xs-12" role="main">
				<noscript>
					<div class="alert alert-danger">
						<strong>JavaScript must be enabled for this program to function properly.</strong>
					</div>
				</noscript>				
				<fieldset>
					<form:form class="form-horizontal hidden" action="submitRegister" method="POST" commandName="regForm">
						
						<p class="smallLeftPadding">Complete all the fields below and click Continue.</p>
						
						<!-- for client-side errors -->
						<div id="errorDiv"></div>

						<!-- For Spring server-side errors -->
						<c:if test="${isError}">
							<div id="errorContainer" class="alert alert-danger">
								<div class="errorMessage">
									<form:errors path="*" />
									<c:if test="${dupEnrollError}">
										<span>${dupErrorMsg}</span>
									</c:if>
								</div>
							</div>
						</c:if>
						
						<c:if test="${pageLoadError}">
							<div id="pageLoadErrorContainer" class="alert alert-danger">
								<div class="errorMessage">
									<span>${siteError}</span>
								</div>
							</div>
						</c:if>
						
						<!-- Institution -->
						<div class="form-group">
							<form:label path="site" class="control-label col-md-2 col-sm-4">${instLabel}</form:label>
							<div class="col-md-10 col-sm-8">
								<div class="input-group">
									<c:choose>
										<c:when test="${isSingleInst}">
											<span class="blockText">${singleInst} - ${singleInstDesc}</span>
											<input type="hidden" id="site" name="site" value="${singleInst}" />
										</c:when>
										<c:otherwise>
											<form:select path="site" class="form-control input-sm">
												<form:options items="${userSites}" />
											</form:select>
										</c:otherwise>
									</c:choose>									
									<div class="input-group-addon bg-none text-primary">
										<span id="instTooltip"
											class="glyphicon glyphicon-info-sign"
											data-toggle="tooltip" title="" data-placement="right" data-original-title="">
										</span>
									</div>
								</div>
							</div>
						</div>
						
						<!-- Study -->
						
						<div class="form-group">
							<form:label path="study" class="control-label col-md-2 col-sm-4">${studyLabel}</form:label>
							<div class="col-md-10 col-sm-8">
								<div class="input-group">
									<c:choose>
										<%-- Single study text field --%>
										<c:when test="${isSingleStudy}">
											<span class="blockText">${singleStudy}</span>
											<input type="hidden" id="study" name="study" value="${singleStudy}" />
										</c:when>
										<c:otherwise>
											<%-- Multiple study dropdown --%>
											<form:select path="study" class="form-control input-sm">
												<form:options items="${userStudies}" />
											</form:select>
										</c:otherwise>
									</c:choose>
									
									<div class="input-group-addon bg-none text-primary">
										<span id="protToolTip"
											class="glyphicon glyphicon-info-sign"
											data-toggle="tooltip" title="" data-placement="right" data-original-title="">
										</span>
									</div>
								</div>
							</div>
							<input type="hidden" name="cStudy" id="cStudy" />
						</div>
						
						<!-- Version -->			
						
						<c:choose>
							<%-- when page reloads, this can happen after server side error or menu link is clicked, the showing/hiding 
						       of fields depends on the property set in the controller, 'isError' and 'navStep' are set in the 
						       controller depending on how the page is called. navStep is assigned a value if the user navigates to this 
						       step by clicking the menu link --%>
							<c:when test="${isError || not empty navStep}">							
								<c:choose>
									<c:when test="${showVersion}">
										<div id="versionDiv" class="form-group">
									</c:when>
									<c:otherwise>
										<div id="versionDiv" class="form-group doNotDisplay">
									</c:otherwise>
								</c:choose>
							</c:when>
							<c:otherwise>
								<div id="versionDiv" class="form-group">
							</c:otherwise>
						</c:choose>						
							<form:label path="version" class="control-label col-md-2 col-sm-4">${versionLabel}</form:label>
							<div class="col-md-10 col-sm-8">
								<div class="input-group">
									<c:choose>
										<c:when test="${isSingleVersion}">
											<span class="blockText">${singleVerDesc}</span>							
											<input type="hidden" id="version" name="version" value="${singleVersion}" />
											<input type="hidden" id="singleVerShow" name="singleVerShow" value="${showSingleVersion}"/>
										</c:when>
										<c:otherwise>
											<form:select class="form-control input-sm" path="version">
												<form:options items="${version}" />
											</form:select>
										</c:otherwise>
									</c:choose>									
									<div class="input-group-addon bg-none text-primary">
										<span id="verTT" class="glyphicon glyphicon-info-sign" data-toggle="tooltip" title="" data-original-title="${versionTT}"></span>					
									</div>
								</div>
							</div>
							<input type="hidden" name="cVersion" id="cVersion" />
						</div>				
						
						
							<!-- SID -->
							<c:choose>
								<%-- 'isError' and 'navStep' are set in the controller depending on how the page is called. 
							       navStep is assigned a value if the user navigates to this step by clicking the menu link --%>
								<c:when test="${isError || not empty navStep}">
									<c:choose>
										<c:when test="${showSid}">
											<div id="sidDiv" class="form-group">
										</c:when>
										<c:otherwise>
											<div id="sidDiv" class="form-group doNotDisplay">
										</c:otherwise>
									</c:choose>
								</c:when>
								<c:otherwise>
									<div id="sidDiv" class="form-group">
								</c:otherwise>
							</c:choose>
								<form:label path="sid" class="control-label col-md-2 col-sm-4">${sidLabel}</form:label>
								<div class="col-md-10 col-sm-8">
									<div class="input-group">
										<form:input class="form-control input-sm" path="sid"/>
										<div class="input-group-addon text-primary bg-none">
											<span id="sidTT" class="glyphicon glyphicon-info-sign text-primary infoIcon" data-toggle="tooltip" title="" data-original-title="${sidTT}"></span>
										</div>
									</div>
								</div>
							</div>
							
						<!-- Continue button -->
						<div class="form-group">
						  <div class="control-label col-md-2 col-sm-4"></div>
						    <div class="col-md-10 col-sm-8">
								<button type="submit" class="btn btn-default disabled" name="subBtnRegister" id="subBtnRegister" disabled="disabled">Continue</button>
						  	</div>
						</div>
						
						<c:if test="${warnChecklist eq 'show'}">
							<div id="overlay">
								<input type="hidden" name="overlayContainer" /> <!-- Firefox removes empty div so fill with hidden element. This div is needed to create gray-out effect  -->
							</div>
							<div id="warnBoxChecklist" class="panel panel-default warnPopupBox absoluteContainer" aria-labelledby="messageBoxTitle">
								<div class="panel-heading">
									<h2 id="messageBotTitle" class="panel-title">Confirm</h2>
								</div>
								<div class="panel-body">
									<span class="processingMsg">${warnChecklistMsg}</span>
									<br />
									<input type="submit" class="btn btn-default smallRightMargin" name="filledChklist" value="Yes" />
									<input type="submit" class="btn btn-default" name="blankChklist" value="No" />
								</div>
							</div>
						</c:if>
						<div>
							<!-- field to know if all the necessary form fields are printed. Value set in JS. Needed to enable/disable the submit button -->
							<input type="hidden" id="areAllFieldsPrinted" name="areAllFieldsPrinted" value="${allAnswered}"/>
							<!-- field to know if user navigated to the page from the menu link -->
							<input type="hidden" id="navStep" name="navStep" value="${navStep}" />
							<input type="hidden" id="stopEnroll" name="stopEnroll" value="${stopEnrollErr}"/>
						</div>
					</form:form>
					
					<div class="modal fade" id="dupEnrollModal" tabindex="-1" role="dialog" aria-labelledby="dupEnrollLabel">
						<div class="modal-dialog" role="document">
							<div class="modal-content">
								<div class="modal-header bg-danger">
									<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true"></span></button>
									<p class="modal-title text-danger" id="dupEnrollLabel">Duplicate Attempt</p>
								</div>
								<div class="modal-body">
									 ${dupEnrollMsg}
								</div>
								<div class="modal-footer">
									<button class="btn btn-default" data-dismiss="modal">Close</button>
								</div>
							</div>
						</div>
					</div>
										
				</fieldset>                
			</main>
			
		</div>
	</div>

	
</body>
</html>
