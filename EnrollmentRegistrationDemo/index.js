$(document).ready(function(){ 
    
    // tooltip hover text
    $('[data-toggle="tooltip"]').tooltip({
        placement: 'auto right',      
			  container: 'body',
        animated:  'fade'
    });
    
    // enable the Continue button if validation error
    if(($('#errorContainer').length || $('#navStep').val() != "") && $('#areAllFieldsPrinted').val() === "yes") {
        $('#subBtnRegister').removeClass("btn-default disabled").addClass("btn-primary");           
        $('#subBtnRegister').prop("disabled", false);
    }
    
    // hide the title, step, patid and scrnum fields by default. if reloading page after server side checks then do not hide by default.
    if(!($('#errorContainer').length || $('#navStep').val() != "")) {
        $('#studyTitle, #versionDiv, #chkId, #stepDiv, #patidDiv, #scrNumDiv, #cPidDiv1, #cPidDiv2, #cPidDiv3, #sidDiv').hide();
    }
    
    // If single version 
    if($('#singleVerShow').length && $('#singleVerShow').val() === "true") {
        $('#versionDiv').show();
        $('#studyTitle').show();
    }
    // If single step
    if($('#showStep').length && $('#showStep').val() === "true") {
        $('#stepDiv').show();
        $('#areAllFieldsPrinted').val("yes");
        checkRequiredFields();
    }
    
    // In case of single site-study-version-step combination there won't be any Ajax call to show the non-core fields
    // Check and show/hide the fields
    if($('#singleStep').length && $('#singleStep').val() === "true") {
        if($('#showPid').length && $('#showPid').val() === "true") {
            $('#patidDiv').show();
        }
    }
	
	// populate the tooltips
    FSTRF.ajax({
        url: "loadInstProtocolTooltips",
        type: "get",
        datatype: "json",
        cache: false,
        success: function(jsonObj) {   
            $('#instTooltip').attr("data-original-title", jsonObj["instToolTip"]);
            $('#protToolTip').attr("data-original-title", jsonObj["protToolTip"]);
        }, 
        error: function(jsonObj) {
            $('#instTooltip').attr("data-original-title", "");
            $('#protToolTip').attr("data-original-title", "");
        },
        returnSettings: {}
    });
    
    // Ajax call when institution is selected
    $('#site').change(function() {
        var site = $(this).val();
        var data = 'site='+encodeURIComponent(site);
        var errArr = [];
        $('#areAllFieldsPrinted').val("no");
        
        var firstElemFormReset = 1; // First field on form to hide other form elements when changed 
        clearFormFields(firstElemFormReset); 
        removeErrorMsg(); // if there is an old error message being displayed then remove it (set contents of container to empty)     
        
        if(site === "") { // User changes selection to "Please select". Reset study, other fields are wiped out by above clearFormFields method
            $('#study').empty();
        } else {
            FSTRF.ajax({
                url: "loadStudies",
                data: data,
                type: "get",
                datatype: "json",
                cache: false,
                success: function(jsonObj) {                
                    // No need to use parseJSON here since in the service class the Jackson library is used to convert 
                    // a java list to a valid json object which we can directly use here.              
                    $('#study').html('');
                    var arr = [];                
                    for(var key in jsonObj) {
                       arr.push(key);
                    }       
                  
                    if(arr.length == 0) { // no active studies found                 
                        var study = "";
                        var step = "";
                        var input = "study="+encodeURIComponent(study)+"&step="+encodeURIComponent(step);
                        displayErrorMsg(input, errArr);                  
                    }
                    else {                                   
                        $('#study').append('<option value=""> Please select</option>');
                        $.each(jsonObj, function(k, v) {                    
                            $('#study').append('<option value="'+v.study+'">'+v.study+'</option>');
                        });
                    }
                   
                    $('#subBtnRegister').removeClass("btn-primary").addClass("btn-default disabled"); // add/remove appropriate bootstrap classes        
                    $('#subBtnRegister').prop("disabled", true);
                  
                },
                error: function(){
                    console.log("Error: Error in ajax");
                    // TODO: need to handle the error in ajax. Maybe show a dialog box           
                },
                returnSettings: {  
                    //TODO:  need to handle the redirect to the index page. This doesn't work.
                    url: "/apps/cfmx/apps/common/PortalRedirector/perform_redirect.jsp?instance=jee2&target=FILTER&path=/j2ee/Starrs/index"
                }           
            });
        }
    });
	
	function showStepDepFields(jsonObj) {
		if(jsonObj.showPatid) {
			$('#patidDiv').show();
			$('#patidDiv').children('.control-label').empty().append(jsonObj.patidLabel);
			$('#pidTT').attr("data-original-title", jsonObj.tooltipText["PID_TT"]);
		}
		if(jsonObj.showScrNum) {
			$('#scrNumDiv').show();
			$('#scrNumDiv').children('.control-label').empty().append(jsonObj.scrNumLabel);
			$('#scrnumTT').attr("data-original-title", jsonObj.tooltipText["SCRNUM_TT"]);
			$('#scrNumType').prop("value", jsonObj.scrNumType);
		}
		if(jsonObj.showChildPid) {
			$('#cPidDiv1').show();
			$('#cPidDiv1').children('.control-label').empty().append(jsonObj.cPidLabel);
			$('#cpidTT').attr("data-original-title", jsonObj.tooltipText["CPID_TT"]);
		}
		if(jsonObj.showSid) {
			$('#sidDiv').show();
			$('#sidDiv').children('.control-label').empty().append(jsonObj.sidLabel);
			$('#sidTT').attr("data-original-title", jsonObj.tooltipText["SID_TT"]);
		}
	}
}