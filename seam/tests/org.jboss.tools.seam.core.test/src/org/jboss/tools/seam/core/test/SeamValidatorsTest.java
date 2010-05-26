/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.seam.core.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jboss.tools.common.util.FileUtil;
import org.jboss.tools.jsf.JSFModelPlugin;
import org.jboss.tools.jsf.preferences.JSFSeverityPreferences;
import org.jboss.tools.jst.web.kb.validation.IValidator;
import org.jboss.tools.seam.core.ISeamComponent;
import org.jboss.tools.seam.core.ISeamComponentMethod;
import org.jboss.tools.seam.core.ISeamProject;
import org.jboss.tools.seam.core.SeamComponentMethodType;
import org.jboss.tools.seam.core.SeamCorePlugin;
import org.jboss.tools.seam.core.SeamPreferences;
import org.jboss.tools.seam.internal.core.SeamProject;
import org.jboss.tools.seam.internal.core.validation.SeamValidationErrorManager;
import org.jboss.tools.test.util.JUnitUtils;
import org.jboss.tools.test.util.JobUtils;
import org.jboss.tools.test.util.ProjectImportTestSetup;
import org.jboss.tools.tests.AbstractResourceMarkerTest;
import org.jboss.tools.tests.IMarkerFilter;

public class SeamValidatorsTest extends AbstractResourceMarkerTest {

	IProject project = null;
	
	public static SeamMarkerFilter SEAM_MARKER_FILTER = new SeamMarkerFilter();

	public SeamValidatorsTest() {
		super("Seam Validator Tests");
	}

	public SeamValidatorsTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		JobUtils.waitForIdle();
		IResource project = ResourcesPlugin.getWorkspace().getRoot().findMember("SeamWebWarTestProject");
		if(project == null) {
			ProjectImportTestSetup setup = new ProjectImportTestSetup(
					this,
					"org.jboss.tools.seam.core.test",
					"projects/SeamWebWarTestProject",
					"SeamWebWarTestProject");
			project = setup.importProject();
		}
		this.project = project.getProject();
		JobUtils.waitForIdle();
	}
	
	@Override
	protected void tearDown() throws Exception {
		// restore state
		copyContentsFile("src/action/org/domain/SeamWebWarTestProject/session/SubclassTestComponent.java",
				"src/action/org/domain/SeamWebWarTestProject/session/SubclassTestComponent.original");
		
		copyContentsFile("WebContent/WEB-INF/components.xml", "WebContent/WEB-INF/components.original");
		
		copyContentsFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.java",
				"src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.original");
		
		copyContentsFile("src/action/org/domain/SeamWebWarTestProject/session/BbcComponent.java",
				"src/action/org/domain/SeamWebWarTestProject/session/BbcComponent.original");
		
		copyContentsFile("WebContent/JBIDE-1631.xhtml",
				"WebContent/JBIDE-1631.original");
		
		copyContentsFile("src/action/org/domain/SeamWebWarTestProject/entity/abcEntity.java",
				"src/action/org/domain/SeamWebWarTestProject/entity/abcEntity.original");
		
		copyContentsFile("src/action/org/domain/SeamWebWarTestProject/session/Component12.java",
				"src/action/org/domain/SeamWebWarTestProject/session/Component12.original");
		
		copyContentsFile("src/action/org/domain/SeamWebWarTestProject/session/SelectionTest.java",
				"src/action/org/domain/SeamWebWarTestProject/session/SelectionTest.original");
		
		copyContentsFile("src/action/org/domain/SeamWebWarTestProject/session/SelectionIndexTest.java",
				"src/action/org/domain/SeamWebWarTestProject/session/SelectionIndexTest.original");
		
		copyContentsFile("src/action/org/domain/SeamWebWarTestProject/session/ContextVariableTest.java",
				"src/action/org/domain/SeamWebWarTestProject/session/ContextVariableTest.original");
		
		copyContentsFile("WebContent/abcComponent.xhtml",
				"WebContent/abcComponent.original");
		
		copyContentsFile("src/action/org/domain/SeamWebWarTestProject/session/DuplicateComponent.java",
				"src/action/org/domain/SeamWebWarTestProject/session/DuplicateComponent.original");
		
		refreshProject(project);
	}
	
	private void copyContentsFile(String originalName, String newContentName){
		IFile originalFile = project.getFile(originalName);
		IFile newContentFile = project.getFile(newContentName);
		
		copyContentsFile(originalFile, newContentFile);
	}
	
	private void copyContentsFile(IFile originalFile, String newContentName){
		IFile newContentFile = project.getFile(newContentName);
		
		copyContentsFile(originalFile, newContentFile);
	}
	
	private void copyContentsFile(IFile originalFile, IFile newContentFile){
		InputStream is = null;
		try {
			is = newContentFile.getContents();
			originalFile.setContents(is, true, false, null);
		} catch (CoreException e) {
			JUnitUtils.fail("Error during changing '"+originalFile.getFullPath()+"' content to '"+newContentFile.getFullPath()+"'", e);
		} finally {
			if(is!=null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private ISeamProject getSeamProject(IProject project) {
		refreshProject(project);
		
		ISeamProject seamProject = null;
		try {
			seamProject = (ISeamProject)project.getNature(SeamProject.NATURE_ID);
		} catch (Exception e) {
			JUnitUtils.fail("Cannot get seam nature.",e);
		}
		assertNotNull("Seam project is null", seamProject);
		return seamProject;
	}

	/**
	 * Test for https://jira.jboss.org/jira/browse/JBIDE-6176
	 * @throws CoreException 
	 */
	public void testFactory() throws CoreException {
		IFile componentFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/FactoryTest.java");
		copyContentsFile(componentFile, "src/action/org/domain/SeamWebWarTestProject/session/FactoryTest.new");
		refreshProject(project);

		assertMarkerIsNotCreated(componentFile, MARKER_TYPE, "Factory method \"somethings\" with a void return type must have an associated @Out/Databinder", 22);

		copyContentsFile(componentFile, "src/action/org/domain/SeamWebWarTestProject/session/FactoryTest.original");
		refreshProject(project);
	}

	/**
	 * Test for https://jira.jboss.org/jira/browse/JBIDE-784
	 * @throws CoreException 
	 */
	public void testJavaFileOutsideClassPath() throws CoreException {
		IFile file = project.getFile("WebContent/Authenticator.java");
		String[] messages = getMarkersMessage(file, SEAM_MARKER_FILTER);
		assertTrue("Problem marker was found in WebContent/Authenticator.java file. Seam EL validator should not validate it.", messages.length == 0);
	}

	/**
	 * Test for http://jira.jboss.com/jira/browse/JBIDE-1318
	 * @throws CoreException 
	 */
	public void testJBIDE1318() throws CoreException {
		getSeamProject(project);
		IFile testJSP = project.getFile("WebContent/test.jsp");
		testJSP.touch(null);
		JobUtils.waitForIdle();
		assertMarkerIsNotCreated(testJSP, MARKER_TYPE, "actor cannot be resolved");
	}

	public void testVarAttributes() throws CoreException {
		// Test for http://jira.jboss.com/jira/browse/JBIDE-999
		IFile file = project.getFile("WebContent/varAttributes.xhtml");
		int number = getMarkersNumberByGroupName(file, SeamValidationErrorManager.MARKED_SEAM_PROJECT_MESSAGE_GROUP);
		assertEquals("Problem marker was found in varAttributes.xhtml file. Validator did not recognize 'var' attribute.", 0, number);
	}

	public void testMessageBundles() throws CoreException {
		// Test for https://jira.jboss.org/jira/browse/JBIDE-5089
		IFile file = project.getFile("WebContent/messagesValidation.jsp");
		int number = getMarkersNumberByGroupName(file, SeamValidationErrorManager.MARKED_SEAM_PROJECT_MESSAGE_GROUP);
		assertEquals("Problem marker was found in messagesValidation.jsp file. Validator did not recognize a message bundle.", 0, number);
	}

	public void testJiraJbide1696() throws CoreException {
		//getSeamProject(project);

		// Test for http://jira.jboss.com/jira/browse/JBIDE-1696
		IFile subclassComponentFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/SubclassTestComponent.java");
		assertMarkerIsCreated(subclassComponentFile, MARKER_TYPE, "Stateful component \"testComponentJBIDE1696\" must have a method marked @Remove", 25);
		IFile superclassComponentFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/SuperclassTestComponent.java");
		
		copyContentsFile(superclassComponentFile, "src/action/org/domain/SeamWebWarTestProject/session/SuperclassTestComponent.withRemove");
		
		refreshProject(project);
		int number = getMarkersNumberByGroupName(subclassComponentFile, SeamValidationErrorManager.MARKED_SEAM_PROJECT_MESSAGE_GROUP);
		assertTrue("We changed super class of component but it still don't see changes.", number == 0);
	}

	public void testJiraJbide1631() throws CoreException {
		// Test for http://jira.jboss.com/jira/browse/JBIDE-1631
		String jbide1631XHTMLFile = "WebContent/JBIDE-1631.xhtml";
		String jbide1631XHTMLFile2 = "WebContent/JBIDE-1631.1";
		
		assertMarkerIsCreated(jbide1631XHTMLFile, jbide1631XHTMLFile2, "\"foo1\" cannot be resolved", 16 );
		assertMarkerIsCreated(project.getFile(jbide1631XHTMLFile), MARKER_TYPE, "\"foo2\" cannot be resolved", 16 );
	}
	
	public void testDuplicateComponentName_Validator() throws CoreException {
		IFile bbcComponentFile = project
				.getFile("src/action/org/domain/SeamWebWarTestProject/session/BbcComponent.java");

		String markerText = "Duplicate component name: \"abcComponent\"";
		
		assertMarkerIsNotCreated(bbcComponentFile, MARKER_TYPE, markerText);

		// Duplicate component name
		assertMarkerIsCreated(
				"src/action/org/domain/SeamWebWarTestProject/session/BbcComponent.java",
				"src/action/org/domain/SeamWebWarTestProject/session/BbcComponent.2",
				markerText, 7);

		IFile newContentFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/BbcComponent.3");
		IFile targetFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/BbcComponent.java");
		FileUtil.copyContent(newContentFile, targetFile, true, false, null);
		refreshProject(project);
	}
	
	public void testStatefulComponentWithoutRemoveMethod_Validator() throws CoreException {
		IFile statefulComponentFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.java");
		
		String markerText = "Stateful component \"statefulComponent\" must have a method marked @Remove";
		
		assertMarkerIsNotCreated(statefulComponentFile, MARKER_TYPE, markerText);
		
		// Stateful component does not contain @Remove method
		assertMarkerIsCreated(
				"src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.java",
				"src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.2",
				markerText, 16);
	}
	
	public void testStatefulComponentWithoutDestroyMethod_Validator() throws CoreException {
		IFile statefulComponentFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.java");
		
		String markerText = "Stateful component \"statefulComponent\" must have a method marked @Destroy";
		
		assertMarkerIsNotCreated(statefulComponentFile, MARKER_TYPE, markerText);
		
		// Stateful component does not contain @Destroy method
		assertMarkerIsCreated(
				"src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.java",
				"src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.3",
				markerText, 16);
	}
	
	public void testStatefulComponentHasWrongScope_Validator() throws CoreException {
		IFile statefulComponentFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.java");

		String markerText = "Stateful component \"statefulComponent\" should not have org.jboss.seam.ScopeType.PAGE, nor org.jboss.seam.ScopeType.STATELESS";
		
		assertMarkerIsNotCreated(statefulComponentFile, MARKER_TYPE, markerText);
		
		// Stateful component has wrong scope
		
		assertMarkerIsCreated(
				"src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.java",
				"src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.4",
				markerText, 16);
	}
	
	public void testComponentType_Validator() throws CoreException {
		IFile componentsFile = project.getFile("WebContent/WEB-INF/components.xml");
		
		String markerText = "\"org.domain.SeamWebWarTestProject.session.StateComponent\" cannot be resolved to a type";
		
		assertMarkerIsNotCreated(componentsFile, MARKER_TYPE, markerText);
		
		// Component class name cannot be resolved to a type
		
		assertMarkerIsCreated(
				"WebContent/WEB-INF/components.xml",
				"WebContent/WEB-INF/components.2",
				markerText, 15);
	}
	
	public void testComponentWithoutSetter_Validator() throws CoreException {
		IFile componentsFile = project.getFile("WebContent/WEB-INF/components.xml");
		
		// Component class does not contain setter for property
		
		copyContentsFile(componentsFile, "WebContent/WEB-INF/components.3");
		
		copyContentsFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.java", "src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.5");
		
		refreshProject(project);
		
		assertMarkerIsCreated(
				componentsFile,
				MARKER_TYPE,
				"Class \"StatefulComponent\" of component \"statefulComponent\" does not have a setter or a field for the property \"abc\"", 16);
	}

	public void testEntityHasWrongScope_Validator() throws CoreException {
		IFile abcEntityFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/entity/abcEntity.java");
		
		String markerText = "Entity component \"abcEntity\" should not have org.jboss.seam.ScopeType.STATELESS";
		
		assertMarkerIsNotCreated(abcEntityFile, MARKER_TYPE, markerText);
		
		// Entity component has wrong scope
		
		assertMarkerIsCreated(
				"src/action/org/domain/SeamWebWarTestProject/entity/abcEntity.java",
				"src/action/org/domain/SeamWebWarTestProject/entity/abcEntity.2",
				markerText, 15);
	}

	final String TARGET_FILE_NAME 
	= "src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.java";
	
	public void testDuplicateDestroyMethod_Validator() throws CoreException {
		final String NEW_CONTENT_FILE_NAME6 = "src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.6";

		// Duplicate @Destroy method
	
		refreshProject(project);

		assertMarkerIsCreated(
				TARGET_FILE_NAME, 
				NEW_CONTENT_FILE_NAME6, 
				".*\"destroyMethod\".*", 34);
		
		assertMarkerIsCreated(
				TARGET_FILE_NAME, ".*\"destroyMethod2\"", 39);
	}
	
	public void testDuplicateCreateMethod_Validator() throws CoreException {
		// Duplicate @Create method

		final String NEW_CONTENT_FILE_NAME7 = "src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.7";

		assertMarkerIsCreated(
				TARGET_FILE_NAME,NEW_CONTENT_FILE_NAME7, ".*@Create.*\"createMethod\".*", 36);
		assertMarkerIsCreated(
				TARGET_FILE_NAME, ".*@Create.*\"createMethod2\".*", 41);
	}
	
	public void testDuplicateUnwrapMethod_Validator() throws CoreException {
		// Duplicate @Unwrap method

		final String NEW_CONTENT_FILE_NAME8 = "src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.8";
		assertMarkerIsCreated(
				TARGET_FILE_NAME,NEW_CONTENT_FILE_NAME8, ".*@Unwrap.*\"unwrapMethod\".*", 40);
		assertMarkerIsCreated(
				TARGET_FILE_NAME, ".*@Unwrap.*\"unwrapMethod2\".*", 45);
	}
	
	public void testOnlyJavaBeansAndStatefulSessionBeansSupportDestroyMethod_Validator() throws CoreException {
		// Only JavaBeans and stateful session beans support @Destroy methods

		final String NEW_CONTENT_FILE_NAME9 = "src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.9";
		assertMarkerIsCreated(
				TARGET_FILE_NAME,NEW_CONTENT_FILE_NAME9, ".*@Destroy.*\"destroyMethod\".*", 25);
	}
	
	public void testOnlyComponentClassCanHaveCreateMethod_Validator() throws CoreException {
		// Only component class can have @Create method

		final String NEW_CONTENT_FILE_NAME10 = "src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.10";

		assertMarkerIsCreated(
				TARGET_FILE_NAME,NEW_CONTENT_FILE_NAME10, ".*@Create.*\"createMethod\".*", 25);
	}
	
	public void testOnlyComponentClassCanHaveUnwrapMethod_Validator() throws CoreException {
		// Only component class can have @Unwrap method

		final String NEW_CONTENT_FILE_NAME11 = "src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.11";

		assertMarkerIsCreated(
				TARGET_FILE_NAME,NEW_CONTENT_FILE_NAME11, "Only component class can have @Unwrap method \"unwrapMethod\"", 26);
	}
	
	public void testOnlyComponentClassCanHaveObserverMethod_Validator() throws CoreException {
		// Only component class can have @Observer method
		
		final String NEW_CONTENT_FILE_NAME12 = "src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.12";

		assertMarkerIsCreated(
				TARGET_FILE_NAME,NEW_CONTENT_FILE_NAME12, "Only component class can have @Observer method \"observerMethod\"", 26);
	}
	
	public void testDuplicateRemoveMethod_Validator() throws CoreException {
		// Duplicate @Remove method

		final String NEW_CONTENT_FILE_NAME1 = "src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.1";

		assertMarkerIsCreated(
				TARGET_FILE_NAME,NEW_CONTENT_FILE_NAME1, "Duplicate @Remove method \"removeMethod1\"", 18);
		assertMarkerIsCreated(
				TARGET_FILE_NAME,"Duplicate @Remove method \"removeMethod2\"", 22);
	}

	/**
	 * @param statefulComponentFile
	 * @param string
	 * @param i
	 * @throws CoreException 
	 */
	protected void assertMarkerIsCreated(String targetPath, String newContentPath,
			String pattern, int line) throws CoreException {

		IFile newContentFile = project.getFile(newContentPath);
		IFile targetFile = project.getFile(targetPath);
		InputStream is = null;
		try {
			is = newContentFile.getContents();
			targetFile.setContents(is, true, false, null);
		} finally {
			if(is!=null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		refreshProject(project);
		assertMarkerIsCreated(targetFile, MARKER_TYPE, pattern, line);
	}

	/**
	 * @param statefulComponentFile
	 * @param string
	 * @param i
	 * @throws CoreException 
	 */
	protected void assertMarkerIsCreated(String targetPath,
			String pattern, int line) throws CoreException {
		
		IFile targetFile = project.getFile(targetPath);
		assertMarkerIsCreated(targetFile, MARKER_TYPE, pattern, line);
	}

	/**
	 * The validator should check duplicate @Remove methods only in stateful session bean component
	 * This method tests usual component (not stateful sessian bean) with two @Remove methods. It must not have error markers.  
	 */
	public void testDuplicateRemoveMethodInComponent_Validator() {
		getSeamProject(project);
		IFile componentFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/UsualComponent.java");
		int number = getMarkersNumberByGroupName(componentFile, SeamValidationErrorManager.MARKED_SEAM_PROJECT_MESSAGE_GROUP);
		assertEquals("Problem marker was found in UsualComponent.java file", 0, number);
	}

	public void testFactoriesValidator() throws CoreException {
		IFile component12File = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/Component12.java");
		
		String markerText = "Factory method \"messageList2\" with a void return type must have an associated @Out/Databinder";
		
		assertMarkerIsNotCreated(component12File, MARKER_TYPE, markerText);
		
		// Unknown factory name
		
		assertMarkerIsCreated(
				"src/action/org/domain/SeamWebWarTestProject/session/Component12.java",
				"src/action/org/domain/SeamWebWarTestProject/session/Component12.2",
				markerText, 24);
		

		copyContentsFile(component12File, "src/action/org/domain/SeamWebWarTestProject/session/DuplicateFactory");

		refreshProject(project);

		int number = getMarkersNumber(component12File);
		assertEquals("Duplicate factory name markers were not found", 2, number);
	}

	public void testMultipleDataBinder_Validator() throws CoreException {
		IFile selectionTestFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/SelectionTest.java");
		IFile selectionIndexTestFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/SelectionIndexTest.java");
		
		refreshProject(project);
		
		int number = getMarkersNumber(selectionTestFile);
		assertEquals("Problem marker was found in SelectionIndexTest.java", 0, number);
		
		number = getMarkersNumber(selectionIndexTestFile);
		assertEquals("Problem marker was found in SelectionIndexTest.java", 0, number);

		// Multiple data binder
		
		copyContentsFile(selectionTestFile, "src/action/org/domain/SeamWebWarTestProject/session/SelectionTest.2");

		copyContentsFile(selectionIndexTestFile, "src/action/org/domain/SeamWebWarTestProject/session/SelectionIndexTest.2");
		
		refreshProject(project);
		
		number = getMarkersNumber(selectionTestFile);
		assertFalse("Problem marker 'Multiple data binder' was not found", number == 0);

		String[] messages = getMarkersMessage(selectionTestFile, SEAM_MARKER_FILTER);
		assertTrue("Problem marker 'Multiple data binder", messages[0].startsWith("@DataModelSelection and @DataModelSelectionIndex without name of the DataModel requires the only one @DataModel in the component"));

		Integer[] lineNumbers = getMarkersNumbersOfLine(selectionTestFile, SEAM_MARKER_FILTER);
		
		assertTrue("Wrong number of problem markers", lineNumbers.length == messages.length && messages.length == 2);
		
		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 21 || lineNumbers[0] == 24);
		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 21 || lineNumbers[0] == 24);

		number = getMarkersNumber(selectionIndexTestFile);
		assertFalse("Problem marker 'Multiple data binder' was not found", number == 0);
		
		messages = getMarkersMessage(selectionIndexTestFile, SEAM_MARKER_FILTER);
		assertTrue("Problem marker 'Multiple data binder", messages[0].startsWith("@DataModelSelection and @DataModelSelectionIndex without name of the DataModel requires the only one @DataModel in the component"));

		lineNumbers = getMarkersNumbersOfLine(selectionIndexTestFile, SEAM_MARKER_FILTER);
		
		assertTrue("Wrong number of problem markers", lineNumbers.length == messages.length && messages.length == 2);
		
		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 21 || lineNumbers[0] == 24);
		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 21 || lineNumbers[0] == 24);
		
		
	}
	
	public void testUnknownDataModelName_Validator() throws CoreException {
		IFile selectionTestFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/SelectionTest.java");
		IFile selectionIndexTestFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/SelectionIndexTest.java");
		// Unknown @DataModel/@Out name
		
		copyContentsFile(selectionTestFile, "src/action/org/domain/SeamWebWarTestProject/session/SelectionTest.3");

		copyContentsFile(selectionIndexTestFile, "src/action/org/domain/SeamWebWarTestProject/session/SelectionIndexTest.3");
		
		refreshProject(project);
		
		assertMarkerIsCreated(
				selectionTestFile,
				MARKER_TYPE,
				"Unknown @DataModel/@Out name: \"messageList2\"", 27);
		
		assertMarkerIsCreated(
				selectionIndexTestFile,
				MARKER_TYPE,
				"Unknown @DataModel/@Out name: \"messageList2\"", 27);
	}

	public void testDuplicateVariableName_Validator() throws CoreException {
		modifyPreferences();

		IFile contextVariableTestFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/ContextVariableTest.java");
		
		refreshProject(project);
		
		int number = getMarkersNumber(contextVariableTestFile);
		assertEquals("Problem marker was found in contextVariableTestFile.java", 0, number);
		
		// Duplicate variable name
		
		copyContentsFile(contextVariableTestFile, "src/action/org/domain/SeamWebWarTestProject/session/ContextVariableTest.2");
		
		refreshProject(project);
		
		String[] messages = getMarkersMessage(contextVariableTestFile, SEAM_MARKER_FILTER);
		
		assertEquals("Not all problem markers 'Duplicate variable name' was found", 2, messages.length);
		
		for(int i=0;i<2;i++)
			assertEquals("Problem marker 'Duplicate factory name' not found", "Duplicate factory name: \"messageList\"", messages[i]);
		
		Integer[] lineNumbers = getMarkersNumbersOfLine(contextVariableTestFile, SEAM_MARKER_FILTER);
		
		for(int i=0;i<2;i++)
			assertTrue("Problem marker has wrong line number", (lineNumbers[i] == 36)||(lineNumbers[i] == 41));
		
		
	}
	
	public void testUnknownVariableName_Validator() throws CoreException {
		IFile contextVariableTestFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/ContextVariableTest.java");
		
		String markerText = "Unknown context variable name: \"messageList5\"";
		
		assertMarkerIsNotCreated(contextVariableTestFile, MARKER_TYPE, markerText);
		
		// Unknown variable name
		
		assertMarkerIsCreated(
				"src/action/org/domain/SeamWebWarTestProject/session/ContextVariableTest.java",
				"src/action/org/domain/SeamWebWarTestProject/session/ContextVariableTest.3",
				markerText, 22);
	}

	public void testContextVariableCannotBeResolved_Validator() throws CoreException {
		modifyPreferences();

		IFile abcComponentXHTMLFile = project.getFile("WebContent/abcComponent.xhtml");
		
		refreshProject(project);
		
		assertMarkerIsNotCreated(abcComponentXHTMLFile, MARKER_TYPE, "\"bcComponent\" cannot be resolved");
		
		// Context variable cannot be resolved
		
		assertMarkerIsCreated(
				"WebContent/abcComponent.xhtml",
				"WebContent/abcComponent.2",
				"\"bcComponent\" cannot be resolved", 22);
	}
	
	public void testPropertyCannotBeResolved_Validator() throws CoreException {
		IFile abcComponentXHTMLFile = project.getFile("WebContent/abcComponent.xhtml");
		
		String markerText = "\"actionType2\" cannot be resolved";
		
		assertMarkerIsNotCreated(abcComponentXHTMLFile, MARKER_TYPE, markerText);
		
		// Property cannot be resolved
		
		assertMarkerIsCreated(
				"WebContent/abcComponent.xhtml",
				"WebContent/abcComponent.3",
				markerText, 22);
	}
	
	public void testPropertyHasOnlySetter_Validator() throws CoreException {
		IFile abcComponentXHTMLFile = project.getFile("WebContent/abcComponent.xhtml");
		IFile abcComponentFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/AbcComponent.java");

		String markerText = "Property \"actionType\" has only Setter. Getter is missing.";

		assertMarkerIsNotCreated(abcComponentXHTMLFile, MARKER_TYPE, markerText);

		assertMarkerIsNotCreated(abcComponentFile, MARKER_TYPE, markerText);

		// Unpaired Getter/Setter

		enableUnpairGetterOrSetterValidation(true);

		copyContentsFile(abcComponentFile, "src/action/org/domain/SeamWebWarTestProject/session/AbcComponent.2");

		refreshProject(project);

		assertMarkerIsCreated(
				"WebContent/abcComponent.xhtml",
				"WebContent/abcComponent.4",
				markerText, 22);

		enableUnpairGetterOrSetterValidation(false);
	}
	
	public void testPropertyHasOnlyGetter_Validator() throws CoreException {
		IFile abcComponentXHTMLFile = project.getFile("WebContent/abcComponent.xhtml");

		String markerText = "Property \"actionType\" has only Getter. Setter is missing.";

		assertMarkerIsNotCreated(abcComponentXHTMLFile, MARKER_TYPE, markerText);

		enableUnpairGetterOrSetterValidation(true);

		copyContentsFile("src/action/org/domain/SeamWebWarTestProject/session/AbcComponent.java", "src/action/org/domain/SeamWebWarTestProject/session/AbcComponent.3");

		refreshProject(project);

		assertMarkerIsCreated(
				abcComponentXHTMLFile,
				MARKER_TYPE,
				markerText, 22);

		enableUnpairGetterOrSetterValidation(false);
	}

	private void enableUnpairGetterOrSetterValidation(boolean enable) {
		IPreferenceStore store = JSFModelPlugin.getDefault().getPreferenceStore();
		store.putValue(JSFSeverityPreferences.UNPAIRED_GETTER_OR_SETTER, enable?SeamPreferences.ERROR:SeamPreferences.IGNORE);
		if(store instanceof IPersistentPreferenceStore) {
			try {
				((IPersistentPreferenceStore)store).save();
			} catch (IOException e) {
				SeamCorePlugin.getPluginLog().logError(e);
			}
		}
	}

	public void testInheritedMethods() {
		ISeamProject seamProject = getSeamProject(project);

		ISeamComponent c = seamProject.getComponent("inheritedComponent");
		assertNotNull("Component inheritedComponent is not found", c);

		Set<ISeamComponentMethod> ms = c.getMethodsByType(SeamComponentMethodType.DESTROY);
		assertTrue("Seam tools does not see @Destroy-annotated method declared in super class", ms.size() > 0);

		ms = c.getMethodsByType(SeamComponentMethodType.REMOVE);
		assertTrue("Seam tools does not see @Remove-annotated method declared in super class", ms.size() > 0);

		IFile f = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/InheritedComponent.java");
		int errorsCount = getMarkersNumber(f);
		assertEquals("Seam tools validator does not see annotated methods declared in super class", 0, errorsCount);
	}

	// See https://jira.jboss.org/jira/browse/JBIDE-4393
	public void testDuplicateComponents() {
		refreshProject(project);
		IFile duplicateJavaComponentFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/DuplicateComponent.java");
		IFile componentsXmlFile = project.getFile("WebContent/WEB-INF/components.xml");

		copyContentsFile(componentsXmlFile, "WebContent/WEB-INF/duplicateComponents.test");
		
		refreshProject(project);
		
		Integer[] lineNumbers = getMarkersNumbersOfLine(duplicateJavaComponentFile, SEAM_MARKER_FILTER);
		assertEquals("There should be the only one error marker in DuplicateComponent.java.", 1, lineNumbers.length);
		assertEquals("Problem marker has wrong line number", 5, lineNumbers[0].intValue());

		lineNumbers = getMarkersNumbersOfLine(componentsXmlFile, SEAM_MARKER_FILTER);
		assertEquals("There should be two error marker in components.xml.", 2, lineNumbers.length);
		assertTrue("Problem marker was not found on 8 line", findLine(lineNumbers, 8));
		assertTrue("Problem marker was not found on 9 line", findLine(lineNumbers, 9));
	}

	// See https://jira.jboss.org/jira/browse/JBIDE-4515
	public void testRevalidationUnresolvedELs() throws CoreException{
		refreshProject(project);
		JSFModelPlugin.getDefault().getPreferenceStore().setValue(JSFSeverityPreferences.RE_VALIDATE_UNRESOLVED_EL, SeamPreferences.ENABLE);
		JSFModelPlugin.getDefault().getPreferenceStore().setValue(JSFSeverityPreferences.UNKNOWN_EL_VARIABLE_NAME, SeamPreferences.ERROR);

		IFile componentFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/entity/TestElRevalidation.java");
		IFile xhtmlFile = project.getFile("WebContent/testElRevalidation.xhtml");

		copyContentsFile(componentFile, "src/action/org/domain/SeamWebWarTestProject/entity/TestElRevalidation.new");

		refreshProject(project);

		IMarker[] markers = findMarkers(xhtmlFile, IMarker.PROBLEM, "\"testElRevalidation\" cannot be resolved");
		assertEquals("There should be an unresolved EL in testElRevalidation.xhtml.", 1, markers.length);
		

		JSFModelPlugin.getDefault().getPreferenceStore().setValue(JSFSeverityPreferences.RE_VALIDATE_UNRESOLVED_EL, SeamPreferences.DISABLE);
		// Check if the validator was not invoked.
		copyContentsFile(componentFile, "src/action/org/domain/SeamWebWarTestProject/entity/TestElRevalidation.original");

		refreshProject(project);

		markers = findMarkers(xhtmlFile, IMarker.PROBLEM, "\"testElRevalidation\" cannot be resolved");
		assertEquals("There should be an unresolved EL in testElRevalidation.xhtml.", 1, markers.length);

		JSFModelPlugin.getDefault().getPreferenceStore().setValue(JSFSeverityPreferences.RE_VALIDATE_UNRESOLVED_EL, SeamPreferences.ENABLE);
		JSFModelPlugin.getDefault().getPreferenceStore().setValue(JSFSeverityPreferences.UNKNOWN_EL_VARIABLE_NAME, SeamPreferences.IGNORE);
	}

	// See https://jira.jboss.org/browse/JBIDE-6352
	public void testErrorMarkerForEL() throws CoreException{
		refreshProject(project);

		IFile xhtmlFile = project.getFile("WebContent/markerTest.xhtml");

		IMarker[] markers = findMarkers(xhtmlFile, IMarker.PROBLEM, "\"testtt\" cannot be resolved");
		assertEquals("There should be an unresolved EL in markerTest.xhtml for 'testtt' segment.", 1, markers.length);
	}

	public static int getMarkersNumber(IResource resource) {
		return getMarkersNumberByGroupName(resource, SeamValidationErrorManager.MARKED_SEAM_PROJECT_MESSAGE_GROUP);
	}

	private static boolean findLine(Integer[] lines, int number) {
		for (int i = 0; i < lines.length; i++) {
			if(lines[i]==number) {
				return true;
			}
		}
		return false;
	}

	private void modifyPreferences(){
		IPreferenceStore store = SeamCorePlugin.getDefault().getPreferenceStore();
		store.putValue(SeamPreferences.UNKNOWN_VARIABLE_NAME, SeamPreferences.ERROR);

		if(store instanceof IPersistentPreferenceStore) {
			try {
				((IPersistentPreferenceStore)store).save();
			} catch (IOException e) {
				SeamCorePlugin.getPluginLog().logError(e);
			}
		}

		store = JSFModelPlugin.getDefault().getPreferenceStore();
		store.putValue(JSFSeverityPreferences.UNKNOWN_EL_VARIABLE_NAME, SeamPreferences.ERROR);
		store.putValue(JSFSeverityPreferences.UNKNOWN_EL_VARIABLE_PROPERTY_NAME, SeamPreferences.ERROR);
//		store.putValue(JSFSeverityPreferences.UNPAIRED_GETTER_OR_SETTER, SeamPreferences.ERROR);
		store.putValue(JSFSeverityPreferences.RE_VALIDATE_UNRESOLVED_EL, SeamPreferences.ENABLE);

		if(store instanceof IPersistentPreferenceStore) {
			try {
				((IPersistentPreferenceStore)store).save();
			} catch (IOException e) {
				SeamCorePlugin.getPluginLog().logError(e);
			}
		}
	}
	
	private void refreshProject(IProject project){
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
			JobUtils.waitForIdle();
			JobUtils.delay(2000);
		} catch (CoreException e) {
			// ignore
		}
	}

	public static class SeamMarkerFilter implements IMarkerFilter {
		public boolean accept(IMarker marker) {
			String groupName = marker.getAttribute("groupName", null);
			return groupName!=null && (groupName.equals(SeamValidationErrorManager.MARKED_SEAM_PROJECT_MESSAGE_GROUP) || groupName.equals(IValidator.MARKED_RESOURCE_MESSAGE_GROUP));
		}
	}
}