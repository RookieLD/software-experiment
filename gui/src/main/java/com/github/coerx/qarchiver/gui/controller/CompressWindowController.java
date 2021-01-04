package com.github.coerx.qarchiver.gui.controller;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CompressWindowController {
    public Button createButton;
    public Button cancelButton;
    public TextField textFieldForFilename;
    public Button choosePathButton;
    public TextField pathLabel;
    public ToggleButton otherOpinion;
    public CheckBox checkGeneratedFileCheckBox;
    public TextField textFieldForPassword;
    public ChoiceBox<String> fileTypeChoiceBox;
    public TextField cronExpression;
    public CheckBox cronTaskCheckBox;

    @FXML
    public void cronCheckBoxOnAction(Event e){
        if(cronTaskCheckBox.isSelected()){
            cronExpression.setDisable(false);
        }
        else{
            cronExpression.setText("");
            cronExpression.setDisable(true);
        }
    }

    public void backToInitialState(){
        pathLabel.setText("");
        textFieldForFilename.setText("");
        fileTypeChoiceBox.getSelectionModel().select(0);
        otherOpinion.setSelected(false);
        otherOpinion.fireEvent(new ActionEvent());

        checkGeneratedFileCheckBox.setSelected(false);

        textFieldForPassword.setText("");
        textFieldForPassword.setDisable(true);

        cronTaskCheckBox.setSelected(false);
        cronExpression.setText("");
        cronExpression.setDisable(true);
    }
}
