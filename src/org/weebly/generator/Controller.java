package org.weebly.generator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.weebly.generator.components.Configuration;
import org.weebly.generator.components.ConfigurationLoader;
import org.weebly.generator.components.TemplateLoader;
import org.weebly.generator.exceptions.AngularIUnitException;
import org.weebly.generator.forms.CreateFile;
import org.weebly.generator.forms.ErrorDialog;
import org.weebly.generator.services.FileHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Controller Generator for angular.
 * Created by suparngupta on 7/7/14.
 */
public class Controller extends AnAction {
    private static String currentPath = "";
    private static FileHandler fileHandler = new FileHandler();
    private static AnActionEvent e;
    private static Project project;
    private static TemplateLoader templateLoader;
    private static ConfigurationLoader configurationLoader;

    public void actionPerformed(AnActionEvent e) {
        VirtualFile data = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if(data == null){
            showError("Unknown Error", "Please file an issue if you think this is a bug");
            return;
        }
        else{
            currentPath = data.getPath();
        }

        project = e.getData(PlatformDataKeys.PROJECT);
        //System.out.println(currentPath);
        templateLoader = ApplicationManager.getApplication().getComponent(TemplateLoader.class);
        configurationLoader = ServiceManager.getService(ConfigurationLoader.class);
        if(configurationLoader.getState() == null){
            configurationLoader.loadState(new Configuration());
        }
        new CreateFile(this).showDialog();
        Controller.e = e;
    }

    public void createHandler(HashMap<String, String> properties) {
        String fileName = properties.get("fileName"),
                fileType = properties.get("fileType"),
                moduleName = properties.get("moduleName");
        //System.out.println(fileName + " creating at " + currentPath);
        try {

            String mainFileName = getSrcFilename(fileName, fileType);
            String testFileName = getTestFilename(fileName, fileType);
            File mainFile = fileHandler.createFile(mainFileName, currentPath);
            File testFile = fileHandler.createFile(testFileName, currentPath);
            File currentDirectory = new File(currentPath);

            VirtualFile fileByIoFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(currentDirectory);

            if (fileByIoFile != null) {
                fileByIoFile.getChildren();
                fileByIoFile.refresh(false, true);


                fileHandler.writeFileContent(mainFile, getSrcContentByType(fileType,
                        getFilenameWithSuffix(fileName, fileType), moduleName));
                fileHandler.writeFileContent(testFile, getTestContentByType(fileType,
                        getFilenameWithSuffix(fileName, fileType), moduleName));

                //open the file after writing content to it.
                processFile(testFileName);
                processFile(mainFileName);
                //since everything is successful add the module name to the configuration

                if(configurationLoader.getState() != null
                        && configurationLoader.getState().getModuleNameSuggestions() != null
                        && !configurationLoader.getState().getModuleNameSuggestions().contains(moduleName)){
                    configurationLoader.getState().getModuleNameSuggestions().add(moduleName);
                }

            } else {
                System.out.println("File not refreshed");
            }

        } catch (AngularIUnitException ae) {
            showError(ae.getName(), ae.getDescription());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Unknown Error", "Please report in case you think that this is a bug.");
        }
    }

    private void processFile(String filename) {
        File toBeOpened;
        VirtualFile vfToBeOpened;
        toBeOpened = new File(currentPath + "/" + filename);
        vfToBeOpened = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(toBeOpened);
        if (vfToBeOpened != null) {
            FileEditorManager.getInstance(project).openFile(vfToBeOpened, true);
        }
    }

    /**
     * Gets the file name with the required type based on if its a controller or a directive or a service
     *
     * @param fileName the filename
     * @param type     the type of the file
     * @return the file name with file type
     */
    private String getFilenameWithSuffix(String fileName, String type) {
        if (type.equals("Controller")) {
            fileName += "Ctrl";

        } else if (type.equals("Directive")) {
            fileName += "Directive";

        } else if (type.equals("Service")) {
            fileName += "Service";
        }

        return fileName;
    }

    private String getSrcContentByType(String type, String componentName, String moduleName) {
        if (type.equalsIgnoreCase("controller")) {
            String content = templateLoader.getDocTemplates().get("controller") + "\n\n" + templateLoader.getCodeTemplates().get("controller");
            return content.replaceAll("#COMPONENTNAME#", componentName).replaceAll("#MODULENAME#", moduleName);
        } else if (type.equalsIgnoreCase("directive")) {
            String content = templateLoader.getDocTemplates().get("directive") + "\n\n" + templateLoader.getCodeTemplates().get("directive");
            return content.replaceAll("#COMPONENTNAME#", componentName).replaceAll("#MODULENAME#", moduleName);
        } else if (type.equalsIgnoreCase("service")) {
            String content = templateLoader.getDocTemplates().get("service") + "\n\n" + templateLoader.getCodeTemplates().get("service");
            return content.replaceAll("#COMPONENTNAME#", componentName).replaceAll("#MODULENAME#", moduleName);
        }

        return "";
    }

    private String getTestContentByType(String type, String componentName, String moduleName) {
        if (type.equalsIgnoreCase("controller")) {
            String content = templateLoader.getDocTemplates().get("controllerSpec") + "\n\n" + templateLoader.getCodeTemplates().get("controllerSpec");
            return content.replaceAll("#COMPONENTNAME#", componentName).replaceAll("#MODULENAME#", moduleName);
        } else if (type.equalsIgnoreCase("directive")) {
            String content = templateLoader.getDocTemplates().get("directiveSpec") + "\n\n" + templateLoader.getCodeTemplates().get("directiveSpec");
            return content.replaceAll("#COMPONENTNAME#", componentName).replaceAll("#MODULENAME#", moduleName);
        } else if (type.equalsIgnoreCase("service")) {
            String content = templateLoader.getDocTemplates().get("serviceSpec") + "\n\n" + templateLoader.getCodeTemplates().get("serviceSpec");
            return content.replaceAll("#COMPONENTNAME#", componentName).replaceAll("#MODULENAME#", moduleName);
        }

        return "";
    }

    public String getSrcFilename(String baseName, String fileType) {
        return getFilenameWithSuffix(baseName, fileType) + ".js";
    }

    public String getTestFilename(String baseName, String fileType) {
        return getFilenameWithSuffix(baseName, fileType) + "Spec.js";
    }

    /**
     * Displays the error dialog
     *
     * @param title       the title of the error
     * @param description the description of the error
     */
    private void showError(String title, String description) {
        new ErrorDialog(title, description).display();
    }

    /**
     * Checks if the main JS file and test file exists
     *
     * @param fileName the filename
     * @return true if the templates don't exist, otherwise false
     */
    public boolean checkIfFileExists(String fileName) {
        return fileHandler.fileExists(currentPath + "/" + fileName);
    }

    /**
     * Returns the suggestions for the module names
     *
     * @return the list of module names used so far
     */
    public List<String> getModuleNameSuggestions() {
        return configurationLoader.getState() == null
                ? new ArrayList<String>()
                : configurationLoader.getState().getModuleNameSuggestions();
    }
}
