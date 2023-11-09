package com.anass.anass_code_editor;

import com.jfoenix.controls.JFXDecorator;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.fxmisc.richtext.model.TwoDimensional.Bias.Forward;

abstract public class App {
    private static HashMap<String,Image> iconImages;
    private static ArrayList<TreeItem<File>> expandedFolders = new ArrayList<>();
    private static File openedFile;
    private static ArrayList<Tab> openedTabs = new ArrayList<>();
    private static File selectedFile;
    private static CheckMenuItem contextAutoSave;
    private static File currentFile;
    private static File selectedFolder;
    private static File openedFolder;
    private static TreeView<File> filesTree;
    private static TreeItem<File> treeRoot;
    private static JFXDecorator decorator;
    private static String duplicateType = "";
    private static Boolean autoSave = false;
    private static VBox linesCounter;
    private static CodeArea codeArea;
    private static Tab selectedTab;
    private static ArrayList<Tab> editedTabs = new ArrayList<>();
    private static String caret = "Line 1:1";
    private static SplitPane vSplitPane;
    private static HBox terminalPrompt;
    private static MenuBar menuBar;
    private static Terminal terminal;
    private static AnchorPane filesPane;
    private static AnchorPane settingsPane;
    private static VBox displayPane;
    private static double oldVDivPosition;
    private static SplitPane hSplitPane;
    private static TextField searchInput;
    private static  SyntaxHighlighter highlighter;
    private static Settings settings;
    private static Stage stage;
    private static HBox codeAreaHolder;
    private static CheckMenuItem autoSaveItem;
    private static CheckBox intellisenseBox,autoSaveBox,wrapTextBox;
    private static ComboBox<String> fontsPlaceHolder;
    private static Spinner<Integer> fontSizePlaceHolder;
    private static Theme theme;
    private static List<Theme> themes;
    private static VBox themesDivision,terminalContainer;
    private static ScrollPane promptsContainer;
    private static StackPane codeAreaStackPane;
    private static File appDataFolder;
    private static File appConfigFile;
    private static ImageView imageViewer = new ImageView();
    public static ImageView getImageViewer(){
        return imageViewer;
    }
    public static void setImageViewer(ImageView iv){
        imageViewer = iv;
    }
    public static ArrayList<String> getIconsFilesNames() {
        ArrayList<String> names = new ArrayList<>();
        InputStream listFileStream = App.class.getResourceAsStream("/com/anass/anass_code_editor/list.txt");
        if (listFileStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(listFileStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    names.add(line.trim());
                }
            } catch (IOException e) {
                System.err.println("Error reading list.txt: " + e.getMessage());
            }
        } else {
            System.err.println("list.txt not found.");
        }

        return names;
    }
    public static void loadIconImages() {
        iconImages = new HashMap<>();
        ArrayList<String> imageNames= getIconsFilesNames();
            try {
                for (String img : imageNames){
                    String name = img.trim().toLowerCase();
                    if (name.contains(".")) {
                        String resourcePath = "/com/anass/anass_code_editor/assets/languages/" + name;
                        InputStream imageStream = App.class.getResourceAsStream(resourcePath);
                        if (imageStream != null) {
                            iconImages.put(name.substring(0, name.indexOf(".")), new Image(imageStream));
                            imageStream.close();
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
    }

    public static void initSavingThread(){
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        if(App.getAutoSave()){
            executor.scheduleAtFixedRate(App::saveAllFiles, 0, 2, TimeUnit.MINUTES);
        }else{
            executor.shutdown();
        }
    }
    public static List<ProgrammingLanguage> programmingLanguages;
    public static double savedWidth;
    public static double savedHeight;
    private static CodeCompleter completer;
    private static HBox tabsPane;
    private static boolean intellisense;
    public static void exit(){
        saveSettings();
        Thread terminalProcess = App.getTerminal().getTerminalProcess();
        if(terminalProcess != null && terminalProcess.isAlive()){
            terminalProcess.interrupt();
        }
        if(editedTabs.size() > 0){
            Alert requestExitAlert = new Alert(Alert.AlertType.CONFIRMATION);
            requestExitAlert.setTitle("exiting programme");
            requestExitAlert.setHeaderText("are you sure want to exit?");
            requestExitAlert.setContentText("some files might not be saved");
            ButtonType result = requestExitAlert.showAndWait().orElse(ButtonType.CANCEL);
            if(result == ButtonType.OK){
                System.exit(0);
            }
        }
        else System.exit(0);

    }
    public static void minimize(MouseEvent e){
        Stage primaryStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        primaryStage.setIconified(true);
    }
    public static void reduce(MouseEvent e){

        Stage primaryStage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        if(primaryStage.isFullScreen())
        {
            primaryStage.setFullScreen(false);
            primaryStage.setMaximized(false);
            primaryStage.setWidth(savedWidth);
            primaryStage.setHeight(savedHeight);
        }
        if (primaryStage.isMaximized()) {
            primaryStage.setMaximized(false);
            primaryStage.setWidth(savedWidth);
            primaryStage.setHeight(savedHeight);
        } else {
            savedWidth = primaryStage.getWidth();
            savedHeight = primaryStage.getHeight();
            primaryStage.setMaximized(true);
        }
        getSettings().minimized = !primaryStage.isMaximized();

    }
    public static void reduce(boolean m){

        Stage primaryStage = getStage();
        if(primaryStage.isFullScreen())
        {
            primaryStage.setFullScreen(false);
            primaryStage.setMaximized(false);
            primaryStage.setWidth(savedWidth);
            primaryStage.setHeight(savedHeight);
        }
        if (m) {
            primaryStage.setMaximized(false);
            primaryStage.setWidth(savedWidth);
            primaryStage.setHeight(savedHeight);
        } else {
            savedWidth = primaryStage.getWidth();
            savedHeight = primaryStage.getHeight();
            primaryStage.setMaximized(true);
            if(decorator != null)
                decorator.setMaximized(true);
        }

    }
    public static void setEditorPLanguages(List<ProgrammingLanguage> data){
        programmingLanguages = data;
    }
    public static String getExtension(String s){
        if(!s.contains(".")) return "";
        return s.substring(s.lastIndexOf("."));
    }
    public static String getLanguageNameOfExtension(String ext) {
        if(programmingLanguages == null) return "file";
        for (ProgrammingLanguage programmingLanguage : programmingLanguages) {
            String[] extensions = programmingLanguage.extensions;
            if (extensions == null) continue;
            if (Arrays.asList(programmingLanguage.extensions).contains(ext)) {
                return programmingLanguage.name.toLowerCase();
            }
        }
        return "file";
    }
    public static File getCurrentDir(){
        return openedFolder;
    }
    public static void setCurrentDir(File f){
        openedFolder = f;
    }
    public static void setSelectedFolder(File file){
        if(file.isDirectory()) selectedFolder = file;
    }
    public static File getSelectedFolder(){
        return selectedFolder;
    }
    public static void setSelectedFile(File file){
        selectedFile = file;
    }

    public static Image getFileIcon(String fileName){
        String langName = getLanguageNameOfExtension(getExtension(fileName));
        return iconImages.get(langName.toLowerCase());
    }
    public static HashMap<String,Image>  getIcons(){
        return iconImages;
    }


    public static CodeCompleter getCompleter(){return completer;}

    public static ArrayList<TreeItem<File>> getExpandedFolders() {
        return expandedFolders;
    }

    public static void setExpandedFolders(ArrayList<TreeItem<File>> expandedFolders) {
        App.expandedFolders = expandedFolders;
    }
    public static void renderFiles(File directory,TreeItem<File> root){
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    TreeItem<File> fNode = new TreeItem<>(file);
                    ArrayList<TreeItem<File>> expanded = App.getExpandedFolders();
                    fNode.setExpanded(expanded.stream().anyMatch(n -> n.getValue().getName().equals(fNode.getValue().getName())));
                    root.getChildren().add(fNode);
                    if(file.isDirectory()){
                        renderFiles(file,fNode);
                    }
                }
            }
        } else {
            System.out.println("Directory does not exist or is not a directory.");
        }
    }
    public static void renderFilesTree(){
        File currentDir = getCurrentDir();
        treeRoot = new TreeItem<>(currentDir);
        treeRoot.setExpanded(true);
        filesTree.setCellFactory(param -> new AppTreeCell());
        filesTree.setRoot(treeRoot);
        renderFiles(currentDir,treeRoot);
    }
    public static void addFile()  {
        try{
            File currentFolder = getSelectedFolder();
            if(currentFolder == null){
                currentFolder = getTreeRoot().getValue();
            }
            if(currentFolder == null) return;
            String name = prompt("file name" , "please enter file name");
            if(!name.isEmpty()){
                File fich = new File(currentFolder.getAbsolutePath() + "\\"+ name);
                int i = 1;
                while(fich.exists()){
                    fich = new File(currentFolder.getAbsolutePath() + "\\"+"("+i+") "+ name);
                    i++;
                }
                fich.createNewFile();
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void addFolder(){
        try{
            File currentFolder = getSelectedFolder();
            if(currentFolder == null){
                currentFolder = getTreeRoot().getValue();
            }
            if(currentFolder == null) return;
            String name = prompt("folder name" , "please enter folder name");
            if(!name.isEmpty()){
                File fich = new File(currentFolder.getAbsolutePath() + "\\"+ name);
                int i = 1;
                while(fich.exists()){
                    fich = new File(currentFolder.getAbsolutePath() + "\\"+"("+i+") "+ name);
                    i++;
                }
                fich.mkdir();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    private static void renameFile(ActionEvent actionEvent) {
        if(selectedFile == null) return;
        int i = 1;
        String newName = prompt("enter new name" , "please enter new name");
        if(newName.isEmpty()) return;
        File newFile = new File(selectedFile.getAbsolutePath().replace(selectedFile.getName(),newName));
        while(newFile.exists()){
            newFile = new File(selectedFile.getAbsolutePath().replace(selectedFile.getName(),"("+i+") "+newName));
            i++;
        }
        boolean isFileOpen = openedTabs.stream().anyMatch(t -> t.getFile().getName().equals(selectedFile.getName()));
        selectedFile.renameTo(newFile);
        if(isFileOpen){
            Tab relatedTab = openedTabs.stream().filter(t -> t.getFile().getName().equals(selectedFile.getName())).findAny().get();
            ImageView icon = (ImageView) relatedTab.getChildren().get(0);
            Label label = (Label) relatedTab.getChildren().get(1);
            icon.setImage(getFileIcon(newFile.getName()));
            label.setText(newFile.getName());
            relatedTab.setFile(newFile);
        }
        renderFilesTree();
        updateInfoBar();
    }
    private static void newFile(ActionEvent actionEvent) {
        if(selectedFolder == null) selectedFolder = getTreeRoot().getValue();
        if(selectedFolder == null) return;
        addFile();
        renderFilesTree();
    }
    private static void newFolder(ActionEvent actionEvent) {
        if(selectedFolder == null) selectedFolder = getTreeRoot().getValue();
        if(selectedFolder == null) return;
        addFolder();
        renderFilesTree();
    }
    public static boolean requestConfirmation(){
        Alert request = new Alert(Alert.AlertType.CONFIRMATION);
        request.setContentText("do you confirm your action ?");
        request.setTitle("please confirm");
        Optional<ButtonType> response = request.showAndWait();
        return response.filter(buttonType -> buttonType == ButtonType.OK).isPresent();
    }
    public static String prompt(String message,String cont){
        TextInputDialog request = new TextInputDialog();
        request.setContentText(cont);
        request.setTitle(message);
        if(selectedFile != null){
            request.getEditor().setText(selectedFile.getName());
        }
        Optional<String> response = request.showAndWait();
        return response.orElse("");
    }
    public static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (!Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        file.delete();
    }
    private static void deleteFile(ActionEvent actionEvent) {
        if(selectedFile == null) return;
        if(requestConfirmation()){
            if(selectedFile.isFile()){
                selectedFile.delete();
            }
            else if(selectedFile.isDirectory()){
                deleteDir(selectedFile);
            }
        }
        selectedFile = null;
        selectedFolder = getTreeRoot().getValue();
        renderFilesTree();
    }
    private static void copyFile(ActionEvent actionEvent) {
        if(selectedFile == null) return;
        duplicateType = "copy";
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putFiles(List.of(selectedFile));
        clipboard.setContent(content);
    }
    private static void pastFile(ActionEvent actionEvent) {
        if(selectedFile == null || selectedFolder == null) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        int i = 1;
        if (clipboard.hasFiles()) {
            List<File> files = clipboard.getFiles();
            for (File file : files) {
                if(duplicateType.equals("cut")){
                    File targetFile = new File(selectedFolder, file.getName());
                    while (targetFile.exists()) {
                        targetFile = new File(selectedFolder,"("+i+") "+ file.getName());
                        i++;
                    }

                    file.renameTo(targetFile);
                    Optional<Tab> relatedTab = openedTabs.stream().filter(t -> t.getFile().getName().equals(selectedFile.getName())).findAny();
                    if(relatedTab.isPresent()) relatedTab.get().setFile(targetFile);
                }
                else if(duplicateType.equals("copy")){
                    File targetFile = new File(selectedFolder, file.getName());
                    while (targetFile.exists()) {
                        targetFile = new File(selectedFolder,"("+i+") "+ file.getName());
                        i++;
                    }
                    try {
                        Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Handle any errors that might occur during the copy operation
                    }
                }

            }
        }
        renderFilesTree();
    }
    private static void cutFile(ActionEvent actionEvent) {
        if(selectedFile == null) return;
        duplicateType = "cut";
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putFiles(List.of(selectedFile));
        clipboard.setContent(content);
    }
    private static FileWriter writer = null;
    private static void saveFile(ActionEvent actionEvent) {
        try{
            if(writer != null)writer.close();
            if(selectedTab == null) return;
            Tab.getFileSavedPlaceHolder().setText("saved");
            writer = new FileWriter(selectedTab.getFile());
            if(!selectedTab.getFile().canWrite()) {
                System.out.println("can't write");
                writer.close();
                return;
            }
            writer.write(selectedTab.getFileContent());
            writer.close();
            selectedTab.setIsSaved(true);
            selectedTab.setEncoding(selectedTab.getEncoding());
            editedTabs.remove(selectedTab);
            writer.close();
            updateInfoBar();
        }catch (IOException e){
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException(e);
        }
        updateInfoBar();
    }
    public static void saveAllFiles(ActionEvent actionEvent) {

        try{
            FileWriter writer;
            for (Tab tab : editedTabs) {
                if(tab == null) continue;
                writer = new FileWriter(tab.getFile());
                writer.write(tab.getFileContent());
                writer.close();
                tab.setIsSaved(true);
            }
            Platform.runLater(App::updateInfoBar);
            editedTabs.clear();
        }catch (Exception e){
                e.printStackTrace();
            }
    }

    private static void toggleAutoSave(ActionEvent actionEvent) {
        autoSave = !autoSave;
        getSettings().autoSave = autoSave;
        autoSaveItem.setSelected(autoSave);
        autoSaveBox.setSelected(autoSave);
        contextAutoSave.setSelected(autoSave);
        initSavingThread();
    }
    public static void openFolder(){
        DirectoryChooser f = new DirectoryChooser();
        f.setTitle("Select Folder");
        File folder = f.showDialog(null);
        if(folder != null && folder.isDirectory()) {
            App.setCurrentDir(folder);
            App.setSelectedFolder(folder);
            getSettings().openedFolder = getCurrentDir().getAbsolutePath();
            Terminal terminal = new Terminal(terminalContainer,promptsContainer);
            App.setTerminal(terminal);
            App.renderFilesTree();
        }
    }
    public static Tab createTab(){
        if(selectedFile == null) return null;
        Tab tab = new Tab(selectedFile,getLanguageNameOfExtension(getExtension(selectedFile.getName())));
        Tab.setWrapper(getTabsPane());
        tab.create();
        return tab;
    }
    public static boolean isTabOpened(Tab tab){
        if(tab.getFile() == null) return false;
        return openedTabs.stream().anyMatch(t -> t.getFile().getAbsolutePath().equals(tab.getFile().getAbsolutePath()));
    }
    public static boolean isBinaryFile(File file) throws IOException {
        if(!file.exists()) return true;
        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead;
            while ((bytesRead = fis.read()) != -1) {
                if (bytesRead < 32 && bytesRead != 9 && bytesRead != 10 && bytesRead != 13) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static void updateSettings(){
        if(openedTabs.size() == 0) return;
        String[] settOpenedTabs = new String[openedTabs.size()];
        for (int i = 0; i < openedTabs.size(); i++) {
            settOpenedTabs[i] = openedTabs.get(i).getFile().getAbsolutePath();
        }
        getSettings().openedTabs = settOpenedTabs;
    }
    public static void openTab(Tab tab)  {
        ObservableList<Node> children = codeAreaStackPane.getChildren();
        children.removeIf(node -> node.getStyleClass().contains("image"));
        codeAreaStackPane.setAlignment(Pos.TOP_LEFT);
        String fileType = "";
        if (tab == null) {
            codeArea.clear();
            updateSettings();
            updateInfoBar();
            codeArea.setEditable(false);
            return;
        }
        if(!tab.getFile().exists()) {
            tab.close();
            codeArea.clear();
            return;
        }
       fileType = getLanguageNameOfExtension(getExtension(tab.getFile().getName()));
        codeArea.setEditable(true);
        if (selectedTab != null) {
            selectedTab.getStyleClass().remove("selected");
        }
        boolean isAlreadyOpened = isTabOpened(tab);
        if (!isAlreadyOpened) {
            openedTabs.add(tab);
            setSelectedTab(tab);
            updateSettings();
        }
        try{
            if(fileType.equals("image")){
                codeArea.setEditable(false);
                imageViewer.getStyleClass().add("image");
                imageViewer.setPreserveRatio(true);
                imageViewer.setFitWidth(codeAreaStackPane.getWidth() - 10);
                imageViewer.setFitHeight(codeAreaStackPane.getHeight() - 10);
                imageViewer.setSmooth(true);
                imageViewer.setCache(true);

                Image image = new Image(tab.getFile().toURI().toURL().openStream());
                imageViewer.setImage(image);
                codeAreaStackPane.setAlignment(Pos.CENTER);
                codeAreaStackPane.getChildren().add(imageViewer);
            }
            else if(isBinaryFile(tab.getFile())){
                codeArea.clear();
                codeArea.replaceText(0,0,"File may contains binary data or non printable chars\nnot editable");
                codeArea.setEditable(false);
                setSelectedTab(tab);
                updateInfoBar();
                return;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        codeArea.clear();
        codeArea.replaceText(0,0,tab.getFileContent());
        setSelectedTab(tab);
        updateInfoBar();
    }
        public static void updateInfoBar(){
        String l,lc,e;
        boolean s;
        if(selectedTab == null){
            l = "not detected";
            lc = "Line 1:1";
            e = "none";
            s = false;
        }
        else{
            l = selectedTab.getLanguage();
            lc = caret;
            e = selectedTab.getEncoding();
            s = selectedTab.isSaved();
        }
        Tab.getFileLanguagePlaceHolder().setText(l);
        Tab.getEncodingPlaceHolder().setText(e);
        Tab.getCurrentLinePlaceHolder().setText(lc);
        if(s){
            Tab.getFileSavedPlaceHolder().setText("saved");
        }else {
            Tab.getFileSavedPlaceHolder().setText("not saved");
        }
    }
    public static void resetDefaultTab(Tab tab){
        if(tab == null || selectedTab == null) return;
        if(tab.getFile().getName().equals(selectedTab.getFile().getName())){
            codeArea.clear();
            caret = "Line 1:1";
            int index = getOpenedTabs().indexOf(selectedTab);
            int newIndex = index + 1;
            if(index > 0)
                newIndex = index - 1;
            if(newIndex < getOpenedTabs().size())
            {
                setSelectedTab(getOpenedTabs().get(newIndex));
            }
            else setSelectedTab(null);
            openTab(selectedTab);
        }
        updateInfoBar();
    }
    public static void nextTab(){
        if(selectedTab == null) return;
        int ind = openedTabs.indexOf(selectedTab);
        ind++;
        if(ind > openedTabs.size() - 1){
            ind = 0;
        }
        Tab newTab = openedTabs.get(ind);
        setSelectedTab(newTab);
        openTab(newTab);
    }
    public static void prevTab(){
        if(selectedTab == null) return;
        int ind = openedTabs.indexOf(selectedTab);
        ind--;
        if(ind < 0){
            ind = openedTabs.size() -1;
        }
        Tab newTab = openedTabs.get(ind);
        setSelectedTab(newTab);
        openTab(newTab);
    }

    public static void initNewTab(ActionEvent actionEvent) {
        if(selectedFile == null) return;
        if(selectedFile.isDirectory()) return;
        if(selectedTab != null) selectedTab.getStyleClass().remove("selected");
        Tab tab = createTab();
        if (tab == null) return;
        setSelectedTab(tab);
        if (isTabOpened(tab)) {
            Tab tb = openedTabs.stream().filter(t -> t.getFile().getName().equals(tab.getFile().getName())).findAny().orElse(null);
            openTab(tb);
            return;
        }
        tabsPane.getChildren().add(tab);
        openTab(tab);
    }
    public static void initContextMenu(Scene root){
        ContextMenu contextMenu = new ContextMenu();
        MenuItem rename = new MenuItem("rename");
        MenuItem open = new MenuItem("open folder");
        MenuItem openFile = new MenuItem("open");
        MenuItem saveFile = new MenuItem("save");
        MenuItem saveAllFiles = new MenuItem("save all");
        contextAutoSave = new CheckMenuItem("auto save");
        MenuItem addFile = new MenuItem("new file");
        SeparatorMenuItem separator_1 = new SeparatorMenuItem();
        SeparatorMenuItem separator_2 = new SeparatorMenuItem();
        MenuItem addFolder= new MenuItem("new folder");
        MenuItem copy = new MenuItem("copy");
        MenuItem cut = new MenuItem("cut");
        MenuItem past = new MenuItem("past");
        MenuItem delete = new MenuItem("delete");
        KeyCodeCombination nextTabCombination = new KeyCodeCombination(KeyCode.TAB,KeyCodeCombination.CONTROL_DOWN);
        KeyCodeCombination prevTabCombination = new KeyCodeCombination(KeyCode.TAB,KeyCodeCombination.CONTROL_DOWN,KeyCodeCombination.SHIFT_DOWN);
        KeyCodeCombination renameCombination = new KeyCodeCombination(KeyCode.R,KeyCodeCombination.CONTROL_DOWN);
        KeyCodeCombination saveFileCombination = new KeyCodeCombination(KeyCode.S, KeyCodeCombination.CONTROL_DOWN);
        KeyCodeCombination saveAllFillesCombination = new KeyCodeCombination(KeyCode.S, KeyCodeCombination.CONTROL_DOWN,KeyCodeCombination.SHIFT_DOWN);
        KeyCodeCombination copyCombination = new KeyCodeCombination(KeyCode.C, KeyCodeCombination.CONTROL_DOWN);
        KeyCodeCombination cutCombination = new KeyCodeCombination(KeyCode.X, KeyCodeCombination.CONTROL_DOWN);
        KeyCodeCombination pastCombination = new KeyCodeCombination(KeyCode.V, KeyCodeCombination.CONTROL_DOWN);
        KeyCodeCombination newFileCombination = new KeyCodeCombination(KeyCode.N, KeyCodeCombination.CONTROL_DOWN);
        KeyCodeCombination newFolderCombination = new KeyCodeCombination(KeyCode.N, KeyCodeCombination.CONTROL_DOWN,KeyCodeCombination.SHIFT_DOWN);
        KeyCodeCombination deleteCombination = new KeyCodeCombination(KeyCode.DELETE);
        KeyCodeCombination openFolderCombination = new KeyCodeCombination(KeyCode.O,KeyCodeCombination.CONTROL_DOWN);
        contextAutoSave.setSelected(App.autoSave);
        contextAutoSave.setOnAction(App::toggleAutoSave);
        open.setAccelerator(openFolderCombination);
        open.setOnAction(e -> openFolder());
        rename.setAccelerator(renameCombination);
        rename.setOnAction(App::renameFile);
        saveFile.setAccelerator(saveFileCombination);
        saveFile.setOnAction(App::saveFile);
        saveAllFiles.setAccelerator(saveAllFillesCombination);
        saveAllFiles.setOnAction(App::saveAllFiles);
        addFile.setAccelerator(newFileCombination);
        addFile.setOnAction(App::newFile);
        addFolder.setAccelerator(newFolderCombination);
        addFolder.setOnAction(App::newFolder);
        cut.setAccelerator(cutCombination);
        cut.setOnAction(App::cutFile);
        copy.setAccelerator(copyCombination);
        copy.setOnAction(App::copyFile);
        past.setAccelerator(pastCombination);
        past.setOnAction(App::pastFile);
        delete.setAccelerator(deleteCombination);
        delete.setOnAction(App::deleteFile);
        openFile.setOnAction(App::initNewTab);
        root.setOnKeyPressed(event -> {
            try{
                if(saveFileCombination.match(event)) saveFile(new ActionEvent());
                if(saveAllFillesCombination.match(event)) saveAllFiles(new ActionEvent());
                if(newFileCombination.match(event)) newFile(new ActionEvent());
                if(newFolderCombination.match(event)) newFolder(new ActionEvent());
                if(deleteCombination.match(event)) deleteFile(new ActionEvent());
                if(copyCombination.match(event)) copyFile(new ActionEvent());
                if(cutCombination.match(event)) cutFile(new ActionEvent());
                if(pastCombination.match(event)) pastFile(new ActionEvent());
                if(renameCombination.match(event)) renameFile(new ActionEvent());
                if(openFolderCombination.match(event)) openFolder();
                if(nextTabCombination.match(event)) nextTab();
                if(prevTabCombination.match(event)) prevTab();
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        });
        contextMenu.getItems().addAll(openFile,open,addFile,addFolder,saveFile,saveAllFiles,contextAutoSave,separator_2,copy,cut,past,separator_1,rename,delete);
        contextMenu.getStyleClass().add("app-context-menu");
        root.setOnContextMenuRequested(event -> contextMenu.show(root.getWindow(), event.getScreenX(), event.getScreenY()));
    }

    public static TreeView<File> getFilesTree() {
        return filesTree;
    }

    public static void setFilesTree(TreeView<File> filesTree) {
        App.filesTree = filesTree;
    }

    public static TreeItem<File> getTreeRoot() {
        return treeRoot;
    }

    public static void setTreeRoot(TreeItem<File> treeRoot) {
        App.treeRoot = treeRoot;
    }
    public static boolean searchFile(String query, TreeItem<File> root) {
        if (root == null || getTreeRoot() == null) return false;
        ObservableList<TreeItem<File>> items = root.getChildren();
        File rootFile = root.getValue();
        items.clear();
        if (rootFile.getName().equals(query)) {
            root.setExpanded(true);
            return true;
        }
        boolean found = false;

        for (File item : rootFile.listFiles()) {
            TreeItem<File> itemNode = new TreeItem<>(item);
            if (item.getName().toLowerCase().trim().contains(query)) {
                items.add(itemNode);
                if(item.isDirectory()) renderFiles(item,itemNode);
                found = true;
            } else {
                if (item.isDirectory() && item.exists()) {
                    if (searchFile(query, itemNode)) {
                        itemNode.setExpanded(true);
                        renderFiles(item, itemNode);
                        items.add(itemNode);
                        found = true;
                    }
                }
            }
        }

        return found;
    }

    public static void handleSearchFile(String query, TreeItem<File> root){
        if(query.trim().length() == 0)
            renderFilesTree();
        else{
            renderFilesTree();
            searchFile(query, root);
        }
    }


    public static Boolean getAutoSave() {
        return autoSave;
    }

    public static void setAutoSave(Boolean autoSave) {
        App.autoSave = autoSave;
        getSettings().autoSave = autoSave;
        initSavingThread();
    }

    public static  HBox getTabsPane() {
        return tabsPane;
    }

    public static void setTabsPane(HBox pane) {
        tabsPane = pane;
    }

    public static VBox getLinesCounter() {
        return linesCounter;
    }

    public static void setLinesCounter(VBox linesCounter) {
        App.linesCounter = linesCounter;
    }

    public static CodeArea getCodeArea() {
        return codeArea;
    }

    public static void setCodeArea(CodeArea codeArea) {
        App.codeArea = codeArea;
    }

    public static ArrayList<Tab> getOpenedTabs() {
        return openedTabs;
    }

    public static void setOpenedTabs(ArrayList<Tab> openedTabs) {
        App.openedTabs = openedTabs;
    }

    public static Tab getSelectedTab() {
        return selectedTab;
    }

    public static void setSelectedTab(Tab tab) {
        App.selectedTab = tab;
        if(tab == null) return;
        getSettings().selectedTab = tab.getFile().getName();
        for (Tab t : openedTabs) {
            t.getStyleClass().remove("selected");
        }
        App.selectedTab.getStyleClass().add("selected");
        syntaxHighlight();
    }

    public static ArrayList<Tab> getEditedTabs() {
        return editedTabs;
    }

    public static void setEditedTabs(ArrayList<Tab> editedTabs) {
        App.editedTabs = editedTabs;
    }
    public static void updateTabText(KeyEvent e){
        if(selectedTab == null) return;
        selectedTab.setFileContent(codeArea.getText());
        if(!editedTabs.contains(selectedTab)) editedTabs.add(selectedTab);
        selectedTab.setIsSaved(false);
        updateInfoBar();
    }
    public static void startCodeArea(){
        codeArea.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
            int caretPosition = newValue.intValue();
            int caretLine = codeArea.offsetToPosition(caretPosition, Forward).getMajor();
            int caretColumn = codeArea.offsetToPosition(caretPosition, Forward).getMinor();
            caret = "Line "+caretLine + ":"+caretColumn;
            Tab.getCurrentLinePlaceHolder().setText(caret);
        });
        codeArea.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue){
                if(autoSave){
                    saveFile(new ActionEvent());
                }
            }
        });
        codeArea.addEventFilter(KeyEvent.KEY_PRESSED,(e) -> {
            if(e.getCode() == KeyCode.TAB && !e.isControlDown()){
                e.consume();
                codeArea.insertText(codeArea.getCaretPosition()," ".repeat(4));
            }
        });
    }

    public static void saveAllFiles() {
        saveAllFiles(new ActionEvent());
    }

    public static void setvSplitPane(SplitPane downSplitPane) {
        vSplitPane = downSplitPane;
    }

    public static SplitPane getvSplitPane() {
        return vSplitPane;
    }

    public static HBox getTerminalPrompt() {
        FXMLLoader promptLoader = new FXMLLoader(App.class.getResource("prompt.fxml"));
        try {
            setTerminalPrompt(promptLoader.load());
        } catch (IOException e) {
            e.printStackTrace();;
        }
        return terminalPrompt;
    }

    public static void setTerminalPrompt(HBox terminalPrompt) {
        App.terminalPrompt = terminalPrompt;
    }

    public static MenuBar getMenuBar() {
        return menuBar;
    }

    public static void setMenuBar(MenuBar menuBar) {
        App.menuBar = menuBar;
    }
    public static void initMenuBar(){
        Menu fileMenu = new Menu("File");
        Menu editMenu = new Menu("Editor");

        MenuItem newFile = new MenuItem("New File");
        //newFile.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        MenuItem newFolder = new MenuItem("New Folder");
        //newFolder.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+N"));
        MenuItem openFile = new MenuItem("Open");
        //openFile.setAccelerator(new KeyCodeCombination(KeyCode.O,KeyCodeCombination.CONTROL_DOWN));
        MenuItem saveFile = new MenuItem("Save");
        //saveFile.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        MenuItem saveAsFile = new MenuItem("Save All");
        //saveAsFile.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+S"));
        MenuItem exit = new MenuItem("Exit");
        exit.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        MenuItem terminalItem = new MenuItem("Terminal");
        terminalItem.setAccelerator(KeyCombination.keyCombination("Ctrl+T"));
        autoSaveItem = new CheckMenuItem("Auto Save");
        fileMenu.getItems().addAll(openFile,newFile,newFolder,new SeparatorMenuItem(),saveFile, saveAsFile,autoSaveItem);
        editMenu.getItems().addAll(terminalItem, exit);
        openFile.setOnAction(e -> openFolder());
        newFile.setOnAction(App::newFile);
        newFolder.setOnAction(App::newFolder);
        saveFile.setOnAction(App::saveFile);
        saveAsFile.setOnAction(App::saveAllFiles);
        autoSaveItem.setOnAction(App::toggleAutoSave);
        exit.setOnAction((e) -> exit());
        terminalItem.setOnAction(e -> getTerminal().maximize());
        autoSaveItem.setSelected(autoSave);
        getMenuBar().getMenus().addAll(fileMenu, editMenu);
    };

    public static Terminal getTerminal() {
        return terminal;
    }

    public static void setTerminal(Terminal terminal) {
        App.terminal = terminal;
    }

    public static AnchorPane getSettingsPane() {
        return settingsPane;
    }

    public static void setSettingsPane(AnchorPane settingsPane) {
        App.settingsPane = settingsPane;
    }

    public static AnchorPane getFilesPane() {
        return filesPane;
    }

    public static void setFilesPane(AnchorPane filesPane) {
        App.filesPane = filesPane;
    }

    public static VBox getDisplayPane() {
        return displayPane;
    }

    public static void setDisplayPane(VBox displayPane) {
        App.displayPane = displayPane;
    }

    public static double getOldVDivPosition() {
        return oldVDivPosition;
    }

    public static void setOldVDivPosition(double oldVDivPosition) {
        App.oldVDivPosition = oldVDivPosition;
    }

    public static void loadSettings() {
        JsonReader<Settings> reader = new JsonReader<>(getAppConfigFile(),Settings.class,"");
        Settings configs = reader.getObject();
        setSettings(configs);
        applySettings();
    }
    public static void loadThemes() {
        JsonReader<Theme> reader = new JsonReader<>("assets/themes.json", Theme.class);
        List<Theme> themes = reader.getData();
        if(themes == null && themes.size() == 0) return;
        setThemes(themes);
        if(settings == null) return;
        setTheme(settings.theme);
        if(codeArea != null) colorizeCodeArea();
        syntaxHighlight();
    }
    public static void applyThemes(){
        getThemesDivision().getChildren().clear();
        for(Theme t : themes){
            HBox root = new HBox();
            root.setAlignment(Pos.CENTER_LEFT);
            root.setMinHeight(Region.USE_PREF_SIZE);
            root.setPrefHeight(47.0);
            root.setPrefWidth(100.0);
            root.setStyle("-fx-background-color: #272727;");
            root.getStyleClass().add("container");
            HBox innerHBox = new HBox();
            innerHBox.setAlignment(Pos.CENTER_LEFT);
            innerHBox.setPrefHeight(100.0);
            Label label = new Label(t.name);
            label.setPrefHeight(42.0);
            label.getStyleClass().add("app_heading");
            innerHBox.getChildren().add(label);
            HBox.setHgrow(innerHBox,Priority.SOMETIMES);
            Button button = new Button("choose");
            button.setOnAction(e ->{
                setTheme(t.name);
            });
            button.setMinWidth(Region.USE_PREF_SIZE);
            button.setPrefHeight(29.0);
            button.setPrefWidth(91.0);
            HBox.setHgrow(button,Priority.ALWAYS);
            button.getStyleClass().add("app_btn");
            root.getChildren().addAll(innerHBox,button);
            getThemesDivision().getChildren().add(root);
        }
    }
    public static void colorizeCodeArea(){
        String th = settings.theme.replace(" ","_").toLowerCase()+"_";
        codeAreaHolder.getStyleClass().add(th+"background");
        codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
            int linesCount = codeArea.getText().split("\n").length;
            codeArea.setParagraphGraphicFactory(lineNumber -> {
                Label label = new Label(String.valueOf(lineNumber + 1));
                label.getStyleClass().add("line-number");
                label.getStyleClass().add(App.getSettings().theme.replace(" ", "_").toLowerCase() + "_line");
                label.setStyle("-fx-pref-width: " + (getNumberCount(linesCount) * settings.fontSize) + "px;"
                        + "-fx-max-width: " + (getNumberCount(linesCount) * settings.fontSize) + "px;"
                        + "-fx-min-width: " + (getNumberCount(linesCount) * settings.fontSize) + "px;");
                return label;
            });
        });

        String text = codeArea.getText();
        codeArea.clear();
        codeArea.replaceText(0,0,text);
    }
    public static int getNumberCount(int number){
        return String.valueOf(number).length();
    }
    public static String getHex(Color color){
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static void  applySettings(){
        if(settings == null) return;
        reduce(settings.minimized);
        codeArea.setWrapText(settings.wrapText);
        codeArea.setStyle("-fx-font-family: '"+settings.fontFamily+"'; -fx-font-size: "+settings.fontSize+"px;");
        savedWidth = settings.windowWidth;
        savedHeight = settings.windowHeight;
        setAutoSave(settings.autoSave);
        setIntellisense(settings.intellisense);
        intellisenseBox.setSelected(getSettings().intellisense);
        intellisenseBox.setOnAction((v) -> {
            settings.intellisense = !intellisense;
            setIntellisense(!intellisense);
        });
        autoSaveBox.setOnAction((v) -> {
            toggleAutoSave(null);
        });
        wrapTextBox.setSelected(getSettings().wrapText);
        if(getCurrentDir() == null){
            File currDir = new File(settings.openedFolder);
            if(currDir.exists() && currDir.isDirectory()){
                setCurrentDir(currDir);
                setSelectedFolder(currDir);
                renderFilesTree();
            }
        }else{
            settings.openedFolder = getCurrentDir().getAbsolutePath();
            renderFilesTree();
        }
        String stab = settings.selectedTab;
        for (String path: settings.openedTabs) {
            File file= new File(path);
            if(file.exists() && file.isFile()){
                String fileType = getLanguageNameOfExtension(getExtension(file.getName()));
                if(!fileType.equals("image")){
                    setSelectedFile(file);
                    initNewTab(null);
                }
            }
        }
        Optional<Tab> st = openedTabs.stream().filter(t -> t.getFile().getName().equals(stab)).findAny();
        st.ifPresent(App::openTab);
        autoSaveBox.setSelected(getSettings().autoSave);
        if(settings.terminalMinimised) {
            getTerminal().minimize();
        }else{
            getTerminal().maximize();
        }
        hSplitPane.getDividers().get(0).setPosition(settings.codeAreaSize);
        vSplitPane.getDividers().get(0).setPosition(settings.terminalSize);
        if(decorator != null){
            decorator.setMaximized(!settings.minimized);
            decorator.setPrefSize(savedWidth,savedHeight);
        }
    }
    public static void saveSettings(){
        File configFile = getAppConfigFile();
        if(configFile == null || !configFile.isFile()) return;
        if(settings == null){
            if(configFile.exists()){
                try {
                    FileWriter writer = new FileWriter(configFile);
                    writer.write("""
                                {
                                  "openedFolder": "",
                                  "openedTabs": [],
                                  "terminalSize": 1,
                                  "codeAreaSize": 0.192,
                                  "autoSave": false,
                                  "intellisense": false,
                                  "wrapText": false,
                                  "minimized": false,
                                  "selectedTab": "",
                                  "windowWidth": 1025,
                                  "windowHeight": 555,
                                  "fontSize": 20,
                                  "fontFamily": "Lucida Console",
                                  "terminalMinimised": true,
                                  "theme": "Default"
                                }""");
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();;
                }
            }
        }else{
            if(configFile.exists()){
                try {
                    FileWriter writer = new FileWriter(configFile);
                    writer.write(getSettings().toString());
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();;
                }
            }
        }

    }
    public static TextField getSearchInput() {
        return searchInput;
    }

    public static void setSearchInput(TextField searchInput) {
        App.searchInput = searchInput;
    }

    public static void syntaxHighlight() {
        if(selectedTab == null) return;
        completer = new CodeCompleter(codeArea,selectedTab.getFile().getName());
        highlighter = new SyntaxHighlighter(codeArea,completer,getLanguageNameOfExtension(getExtension(selectedTab.getFile().getName())));
    }

    public static Settings getSettings() {
        return settings;
    }

    public static void setSettings(Settings settings) {
        App.settings = settings;
    }

    public static Stage getStage() {
        return stage;
    }

    public static void setStage(Stage stage) {
        App.stage = stage;
    }

    public static boolean isIntellisense() {
        return intellisense;
    }

    public static void setIntellisense(boolean intellisense) {
        App.intellisense = intellisense;
    }

    public static SplitPane getHSplitPane() {
        return hSplitPane;
    }

    public static void setHSplitPane(SplitPane hSplitPane) {
        App.hSplitPane = hSplitPane;
    }

    public static CheckBox getIntellisenseBox() {
        return intellisenseBox;
    }

    public static void setIntellisenseBox(CheckBox intellisenseBox) {
        App.intellisenseBox = intellisenseBox;
    }

    public static CheckBox getAutoSaveBox() {
        return autoSaveBox;
    }

    public static void setAutoSaveBox(CheckBox autoSaveBox) {
        App.autoSaveBox = autoSaveBox;
    }

    public static ComboBox<String> getFontsPlaceHolder() {
        return fontsPlaceHolder;
    }

    public static void setFontsPlaceHolder(ComboBox<String> fontsPlaceHolder) {
        App.fontsPlaceHolder = fontsPlaceHolder;
    }

    public static Spinner<Integer> getFontSizePlaceHolder() {
        return fontSizePlaceHolder;
    }

    public static void setFontSizePlaceHolder(Spinner<Integer> fontSizePlaceHolder) {
        App.fontSizePlaceHolder = fontSizePlaceHolder;
    }

    public static ArrayList<Color> getTheme() {
        if(theme == null) return null;
        try{
            ArrayList<Color> colors = new ArrayList<>();
            for(String rgbString : theme.colors){
                String[] rgbValues = rgbString.trim().split("&");
                int r = Integer.parseInt(rgbValues[0]);
                int g = Integer.parseInt(rgbValues[1]);
                int b = Integer.parseInt(rgbValues[2]);
                colors.add(new Color(r,g,b));
            }
            return colors;
        }
        catch (Exception e){
            e.printStackTrace();;
            return null;
        }
    }
    public static ArrayList<Color> getTheme(Theme theme) {
        if(theme == null) return null;
        try{
            ArrayList<Color> colors = new ArrayList<>();
            for(String rgbString : theme.colors){
                String[] rgbValues = rgbString.trim().split("&");
                int r = Integer.parseInt(rgbValues[0]);
                int g = Integer.parseInt(rgbValues[1]);
                int b = Integer.parseInt(rgbValues[2]);
                colors.add(new Color(r,g,b));
            }
            return colors;
        }
        catch (Exception e){
            e.printStackTrace();;
            return null;
        }
    }

    public static void setTheme(String theme) {
        Optional<Theme> th = getThemes().stream().filter(t -> t.name.toLowerCase().equals(theme.toLowerCase())).findAny();
        th.ifPresent(value -> {
            App.theme = value;
            getSettings().theme = theme;
            colorizeCodeArea();
            String the = settings.theme.replace(" ","_").toLowerCase()+"_";
            codeAreaHolder.getStyleClass().clear();
            codeAreaHolder.getStyleClass().add(the+"background");
            syntaxHighlight();
        });
    }

    public static List<Theme> getThemes() {
        return themes;
    }
    public static void setThemes(List<Theme> themes) {
        App.themes = themes;
    }

    public static VBox getThemesDivision() {
        return themesDivision;
    }

    public static void setThemesDivision(VBox themesDivision) {
        App.themesDivision = themesDivision;
    }

    public static HBox getCodeAreaHolder() {
        return codeAreaHolder;
    }

    public static void setCodeAreaHolder(HBox codeAreaHolder) {
        App.codeAreaHolder = codeAreaHolder;
    }

    public static SyntaxHighlighter getHighlighter() {
        return highlighter;
    }

    public static void setHighlighter(SyntaxHighlighter highlighter) {
        App.highlighter = highlighter;
    }

    public static void setWrapTextBox(CheckBox wrapTextBox) {
        App.wrapTextBox = wrapTextBox;
    }

    public static StackPane getCodeAreaStackPane() {
        return codeAreaStackPane;
    }

    public static void setCodeAreaStackPane(StackPane codeAreaStackPane) {
        App.codeAreaStackPane = codeAreaStackPane;
    }

    public static JFXDecorator getDecorator() {
        return decorator;
    }

    public static void setDecorator(JFXDecorator decorator) {
        App.decorator = decorator;
    }

    public static void setPromptsContainer(ScrollPane promptsContainer) {
        App.promptsContainer = promptsContainer;
    }

    public static void setTerminalContainer(VBox terminalContainer) {
        App.terminalContainer = terminalContainer;
    }

    public static File getAppDataFolder() {
        return appDataFolder;
    }

    public static void setAppDataFolder(File appDataFolder) {
        App.appDataFolder = appDataFolder;
    }
    public static File getAppConfigFile() {
        return appConfigFile;
    }

    public static void setAppConfigFile(File appConfigFile) {
        App.appConfigFile = appConfigFile;
    }
}
