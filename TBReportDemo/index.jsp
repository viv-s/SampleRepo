<!DOCTYPE html>
<html lang="en">
<%@ include file="/WEB-INF/views/header.jsp"%>
<main>
	<p>
		Either select a patid or an institution (multiple patids or institutions can be selected too). Enter other report/worksheet filter criteria and click 'Generate report' or 'Generate worksheet'. 
	</p>
	<noscript>
		<div class="formError">JavaScript must be enabled for this program to function properly.</div>
	</noscript>
	<div class="requiredPlugins">
		<p>
			This program requires a PDF reader such as <a target=_blank
				href=http://www.adobe.com/products/acrobat/readstep2.html>Adobe&reg;
				Reader&reg;</a> to view report output.
		</p>
	</div>

	<form:form action="getReport" method="post" id="inputForm" target="_blank" modelAttribute="reportInput">
		<div class="formContent">
			
			<form:errors path="*" cssClass="error" />
			
			 <div class="formRow">
			 	<label for="patid"  title="Filter by patid" class="wide">Patid</label>
			 	<select name="patid" id="patid" class="chosen mediumWidth" multiple="multiple" tabIndex="1">
			 		<c:forEach items="${patids}" var="pid">
			 			<option value="${pid}">${pid}</option>
			 		</c:forEach>
			 	</select>
			 	<small id="patidHelpText" class="hintText">(Leave blank to select all patids)</small>
			 </div>
			
			<div class="formRow">
				<label for="institution" title="Filter by institution" class="wide">Institution</label>
				<select name="institution" id="institution" class="chosen mediumWidth" multiple="multiple" tabIndex="2">
					<c:forEach items="${institutions}" var="inst">
						<option value="${inst}">${inst}</option>
					</c:forEach>
				</select>
				<small id="instHelpText" class="hintText">(Leave blank to select all institutions) </small>
			</div>
			
			<div class="formRow">
				<fieldset>
					<legend>TB Event</legend>
					<div class="formRow">
						<label for="tbEventFromDate"	 title="TB event from date" id="tbFromDate" class="fieldsetLabel">From</label>
						<input	type="text" class="calendar"  id="tbEventFromDate" name="tbEventFromDate" tabIndex="3" />
						<span class="hintText">dd-Mmm-yyyy</span>
					</div>
					<div class="formRow">
						<label for="tbEventFromTo" 	title="TB event to date" id="tbToDate" class="fieldsetLabel">To</label>
						<input type="text" class="calendar"  id="tbEventFromTo" name="tbEventToDate" tabIndex="4" />
						<span class="hintText">dd-Mmm-yyyy</span>
					</div>
				</fieldset>
			</div>			

			<!-- Endpnt2 Disposition -->
			<div class="formRow">
				<label for="disposition" title="Disposition" class="wide">Disposition</label>
				<select name="disposition"  id="disposition" class="chosen largeWidth" multiple="multiple" tabIndex="5">
					<option value="-2" selected="selected">Blank Disposition</option>
					<c:forEach items="${dispositions}" var="item">
						<option value="${item.key}">${item.value}</option>
					</c:forEach>
				</select>			
			</div>

			<!-- Patient filtering -->
			<div class="formRow">
				<fieldset>
					<legend>Patient filtering</legend>

					<div class="formRow">						
						<span id="patidType" class="label fieldsetLabel" title="Select type of participant">Participant type</span>
						<div class="controlColumn" aria-labelledby="patidType">
							<ul class="radioCheck">
								<li>
									<input type="radio" id="household" name="patientType" value="1" tabIndex="6" />
									<label for="household" title="Inlcude household contacts only" >Household contacts</label>
								</li>
								
								<li>
									<input type="radio" id="index" name="patientType" value="2" tabIndex="7"/>
									<label for="index" title="Inlcude index patients only">Index cases</label>
								</li>
								
								<li>
									<input type="radio" id="householdIndex" name="patientType" value="-1" tabIndex="8" checked />
									<label for="householdIndex" title="Inlcude both types">Both</label>
								</li>
							</ul>
						</div>
					</div>
					
					<div class="formRow">
						<span id="studyType" class="label fieldsetLabel" title="Select type of study">Participant status</span>
						<div class="controlColumn" aria-labelledby="studyType">
							<ul class="radioCheck">
								<li>
									<input type="radio" id="onStudy" name="studyStatus" value="2" tabIndex="9" />
									<label for="onStudy" title="On study only">On study</label>
								</li>
								<li>
									<input type="radio" id="offStudy" name="studyStatus" value="1" tabIndex="10" />
									<label for="offStudy" title="Off study only">Off study</label>
								</li>
								<li class="deadPids">
									<input type="checkbox" id="deadPids" name="deadPids"  value="1" tabIndex="11" />
									<label for="deadPids" title="Include dead patids">Include only dead participants</label>
								</li>
								<li>
									<input type="radio" id="allStudyTypes" name="studyStatus" value="-1" tabIndex="12" checked />
									<label for="allStudyTypes" title="Both study types">Both</label>
								</li>								
							</ul>
						</div>
					</div>
					
					<div class="formRow">
						<span id="age" class="label fieldsetLabel" title="Select age range">Age</span>
						<div class="controlColumnWide" aria-labelledby="age">
							<ul class="radioCheck">
								<li>
									<input type="radio" id="below15" name="ageRange" value="&lt;15" tabIndex="13" />
									<label for="below15" title="Participants below 15" >Less than 15</label>
								</li>
								<li>
									<input type="radio" id="above15" name="ageRange" value="&gt;=15" tabIndex="14" />
									<label for="above15" title="Participant above 15" >Greater than or equal to 15</label>
								</li>
								<li>
									<input type="radio" id="bothAges" name="ageRange" value="-1" tabIndex="15" checked />
									<label for="bothAges" title="All ages">Both</label>
								</li>
							</ul>
						</div>
					</div>	
				</fieldset>
			</div>			

			<div class="formRow">				
				<span id="pidDisplayLabelText" class="label wide">Patid display option</span>
				<div class="controlColumn" aria-labelledby="pidDisplayLabelText"> 
					<ul class="radioCheck">
						<li>
							<input type="radio" id="actualPid"	name="patidDisplay" value="1" tabIndex="16" />
							<label for="actualPid"  title="Display actual pids on the report">Actual patid</label>
						</li>
						<li>
							<input type="radio" id="dummyPid" name="patidDisplay" value="2" tabIndex="17" /> 
							<label for="actualPid" title="Display dummy pids on the report">Dummy patid</label>
						</li>
						<li>
							<input type="radio" id="bothPid" name="patidDisplay" value="-1" tabIndex="18" checked />
							<label for="bothPid" title="Display actual and dummy pids on the report">Both</label>
						</li>
					</ul>
				</div>
			</div>

			<div class="formRow">
				<label for="medHistory" title="Select medical history" class="wide">Medical history</label>				
				<form:select path="medHistory" class="form-control" tabIndex="19">
					<form:option value="-1" label="All history" />
					<form:options items="${medHistory}" />
				</form:select>				
			</div>

			<div class="formRow">
				<label for="adverseEvent" title="Select adverse event" class="wide">Adverse event</label>				
				<form:select path="adverseEvent" class="form-control" tabIndex="20">
					<form:option value="-1" label="All events" />
					<form:options items="${adverseEvents}" />
				</form:select>			
			</div>

			<div class="form-group formRow">
				<label for="concomMeds" title="Select concomitant medications" class="wide">Concomitant medication</label>			
				<form:select path="concomMeds" class="form-control" tabIndex="21">
					<form:option value=""  label="All concomitant medications"  />
					<form:options items="${concomMeds}" />
				</form:select>			
			</div>
			
			<div class="formRow">				
				<span id="siteTBLabelText" class="label wide">TB treatment</span>		
				<div class="controlColumn" aria-labelledby="siteTBLabelText">
					<ul class="radioCheck">
						<c:set var="count" value="1" scope="page" />
						<c:forEach var="tbType" items="${tbTreatment}">
							<li>
								<input type="radio" id="tbType${count}"  name="tbTreatment" value="${tbType.key}" tabIndex="22" />
								<label for="tbType${count}" title="${tbType.value}">${tbType.value}</label>
							</li>
							<c:set var="count" value="${count + 1}" scope="page" />					
						</c:forEach>
						<li>
							<input type="radio" id="bothTB" name="tbTreatment" value="bothTB" tabIndex="23" checked />
							<label for="bothTB" title="All TB types">Both</label>
						</li>
					</ul>					
				</div>
			</div>			

			<div id="buttonDiv" class="buttonRowWide">
				<input type="submit" id="submitBtn" name="generateReport" value="Generate report" tabindex="24" />
				<input type="submit" id="worksheetBtn" name="generateWorksheet" value="Generate worksheet" tabindex="25" />
 				<input type="reset" value="Reset" tabindex="26" />
			</div>
		</div>
	</form:form>

</main>
</body>
</html>