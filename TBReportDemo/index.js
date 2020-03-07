$(document).ready(function() {	
	
	$(".deadPids").hide();
	$("#offStudy").click(function(){
		$(".deadPids").slideDown();
	});
	
	$("#onStudy, #allStudyTypes").click(function() {
		$("#deadPids").removeAttr('checked');
		$(".deadPids").hide();
	});	
    
    $('iframe').on('load', function() {
        $("#appBreadcrumbs").hide();
        $(".tickerLoader").remove();
    });
    
});
	

FSTRF.validate = function(formId) {
	var errors = [];

	// Validate that either patids or sites are entered, not both.
	if ($("#patid").val() !== null && $("#institution").val() !== null) {
		if ($("#patid").val().length > 0 && $("#institution").val().length > 0) {
			errors.push(new ValidatorError(
					"Either specify patid or institution, not both.", [
							"patid", "institution" ]));
		}
	}

	// Validate that the TB start date is before the end date (if both are given).
	var startDate = Date.parse($("#tbEventFromDate").val()), endDate = Date
			.parse($("#tbEventFromTo").val());

	if (startDate && endDate && startDate >= endDate) {
		errors.push(new ValidatorError(
				"TB Event start date must precede end date.", [
						"tbEventFromDate", "tbEventFromTo" ]));
	}

	// Cannot enter more than 100 patids (as per DM requirement. 100 is just a safe upper limit. DM doesn't expect this to happen)
	if ($("#patid").val() !== null) {
		if ($("#patid").val().length > 100) {
			alert("Check patids");
			errors.push(new ValidatorError(
					"Exceeded maximum number of patids allowed.", [ "patid" ]));
		}
	}

	return errors;
}

