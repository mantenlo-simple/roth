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
function pwdAjaxResponse(request) {
	if (request.responseText === 'success') {
		Roth.getDialog('changePassword').hide();
		Roth.getDialog('info').flash('The password was successfully changed.');
		getDaysToExpire();
	}
	else
		Roth.getDialog('error').alert('An invalid response was encountered.');
}
function pwdAjaxError(request, forgotten) {
	if (request.status == 404)
		Roth.getDialog('error').alert('You are not logged in, or the session has timed out.');
	else if (request.status == 401)
		Roth.getDialog('error').alert(forgotten ? 'The "Validation Code" supplied is invalid.' : 'The "Old Password" supplied is invalid.');
	else if (request.status == 406)
		Roth.getDialog('error').alert(request.responseText);
	else 
		Roth.getDialog('error').alert('An error occurred while attempting to change the password.<br/>Please notify the system administrator.');
}