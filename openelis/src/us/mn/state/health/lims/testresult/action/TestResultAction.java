/**
* The contents of this file are subject to the Mozilla Public License
* Version 1.1 (the "License"); you may not use this file except in
* compliance with the License. You may obtain a copy of the License at
* http://www.mozilla.org/MPL/ 
* 
* Software distributed under the License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
* License for the specific language governing rights and limitations under
* the License.
* 
* The Original Code is OpenELIS code.
* 
* Copyright (C) The Minnesota Department of Health.  All Rights Reserved.
*/
package us.mn.state.health.lims.testresult.action;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import us.mn.state.health.lims.common.action.BaseAction;
import us.mn.state.health.lims.dictionary.daoimpl.DictionaryDAOImpl;
import us.mn.state.health.lims.dictionary.valueholder.Dictionary;
import us.mn.state.health.lims.testresult.daoimpl.TestResultDAOImpl;
import us.mn.state.health.lims.testresult.valueholder.TestResult;
import us.mn.state.health.lims.typeoftestresult.daoimpl.TypeOfTestResultDAOImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author diane benz
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class TestResultAction extends BaseAction {

    private TypeOfTestResultDAOImpl typeOfTestResultDAO;
    private TestResultDAOImpl testResultDAO;
    private DictionaryDAOImpl dictionaryDAO;
    private final String NUMERIC_TEST_RESULT_TYPE = "N";
    private boolean isNew = false;

    public TestResultAction() {
        typeOfTestResultDAO = new TypeOfTestResultDAOImpl();
        testResultDAO = new TestResultDAOImpl();
        dictionaryDAO = new DictionaryDAOImpl();
    }

    protected ActionForward performAction(ActionMapping mapping,ActionForm form,
                                          HttpServletRequest request,HttpServletResponse response) throws Exception {
		// The first job is to determine if we are coming to this action with an
		// ID parameter in the request. If there is no parameter, we are
		// creating a new TestResult.
		// If there is a parameter present, we should bring up an existing
		// TestResult to edit.
		String id = request.getParameter(ID);

		String forward = FWD_SUCCESS;
		request.setAttribute(ALLOW_EDITS_KEY, "true");
		request.setAttribute(PREVIOUS_DISABLED, "true");
		request.setAttribute(NEXT_DISABLED, "true");

		DynaActionForm dynaForm = (DynaActionForm) form;
        // initialize the form
        dynaForm.initialize(mapping);
		TestResult testResult = new TestResult();

		if (isExistingTestResult(id)) {
			testResult.setId(id);
			testResultDAO.getData(testResult);

			if (testResult.getTest() != null) {
				testResult.setTestName(testResult.getTest().getTestName());
			}
			
			if (testResult.getScriptlet() != null) {
				testResult.setScriptletName(testResult.getScriptlet().getScriptletName());
			}

            String value = testResult.getValue();
            if(value != null && isOfTypeDictionary(testResult.getTestResultType())){
                testResult.setValue(getDictionaryDescription(value));
            }
			isNew = false; // this is to set correct page title
			enableNextAndPreviousButtons(request, testResult);
		} else { // this is a new testResult
			isNew = true; // this is to set correct page title
		}

		if (testResult.getId() != null && !testResult.getId().equals("0")) {
			request.setAttribute(ID, testResult.getId());
		}

		// populate form from valueholder
		PropertyUtils.copyProperties(form, testResult);

		return mapping.findForward(forward);
	}

    private void enableNextAndPreviousButtons(HttpServletRequest request, TestResult testResult) {
        List testResults = testResultDAO.getNextTestResultRecord(testResult.getId());
        if (testResults.size() > 0) {
            request.setAttribute(NEXT_DISABLED, "false");  // enable next button
        }

        testResults = testResultDAO.getPreviousTestResultRecord(testResult.getId());
        if (testResults.size() > 0) {
            request.setAttribute(PREVIOUS_DISABLED, "false");  // enable next button
        }
    }

    private String getDictionaryDescription(String value) {
        Dictionary dictionary = dictionaryDAO.getDataForId(value);
        return dictionary.getDictEntry();
    }

    private boolean isExistingTestResult(String id) {
        return (id != null) && (!"0".equals(id));
    }

    private boolean isOfTypeDictionary(String resultType) {
        return "D".equals(resultType);
    }

    protected String getPageTitleKey() {
		if (isNew) {
			return "testresult.add.title";
		} else {
			return "testresult.edit.title";
		}
	}

	protected String getPageSubtitleKey() {
		if (isNew) {
			return "testresult.add.title";
		} else {
			return "testresult.edit.title";
		}
	}

}
