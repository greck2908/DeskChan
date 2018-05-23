package info.deskchan.gui_javafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class AssetsManagerDialog extends FilesManagerDialog {

	private Path folder;
	private int filesFolderStrLength;
	class FileItem extends File {
		FileItem(String init){ super(init); }
		@Override
		public String toString(){
			if (getAbsolutePath().startsWith(folder.toFile().getAbsolutePath()))
				return getAbsolutePath().substring(filesFolderStrLength);
			return getAbsolutePath();
		}
	}

	private List<String> acceptableExtensions;
	private List<FileItem> selected;
	private String type;


	AssetsManagerDialog(Window parent, String assetsType) {
		super(parent, new ArrayList<>());

		type = assetsType;

		folder = Main.getPluginProxy().getAssetsDirPath().resolve(assetsType);
		System.out.println(folder);
		filesFolderStrLength = folder.toAbsolutePath().toString().length() + 1;

		ObservableList<String> newFiles = FXCollections.observableArrayList();
		for (String file : filesList.getItems())
			newFiles.add(file.substring(filesFolderStrLength));

		filesList.setItems(newFiles);

	}

	@Override
	public List<String> getSelectedFiles(){
		List<String> list = new LinkedList<>();
		System.out.println(filesList.getItems() + " " + filesList.getSelectionModel().getSelectedItems());
		for(String file : filesList.getSelectionModel().getSelectedItems())
			if (file != null)
				list.add(folder.resolve(file).toAbsolutePath().toString());
		return list;
	}

	void setAcceptedExtensions(List<String> extensions){
		acceptableExtensions = extensions;
	}

	void setSelected(List<String> files){
		selected = new ArrayList<>();
		if (files == null) return;
		for (String a : files)
			selected.add(new FileItem(a));
	}

	public void showDialog(){
		List<String> files;

		switch (type){
			case "skins": files = Skin.getSkinList(); break;
			default: files = getFilesList(Main.getPluginProxy().getAssetsDirPath().resolve(type)); break;
		}

		for (String file : files) {
			FileItem item = new FileItem(file);
			filesList.getItems().add(item.toString());
		}


		for (FileItem item : selected) {
			if (!filesList.getItems().contains(item.toString()))
				filesList.getItems().add(item.toString());
			filesList.getSelectionModel().select(item.toString());
		}

		super.showDialog();
	}
	
	private List<String> getFilesList(Path path) {
		List<String> list = new ArrayList<>();
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
			for (Path skinPath : directoryStream) {
				if (Files.isDirectory(skinPath)) {
					list.addAll(getFilesList(skinPath));
					continue;
				}
				String name = skinPath.toString();
				if (acceptableExtensions == null || acceptableExtensions.size() == 0){
					list.add(name);
					continue;
				}
				for (String ext : acceptableExtensions){
					if (name.endsWith(ext)) {
						list.add(name);
						break;
					}
				}
			}
		} catch (IOException e) {
			Main.log(e);
		}
		return list;
	}
	
}