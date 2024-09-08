package ru.edu.spbstu.clientComponents;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Rectangle;
import ru.edu.spbstu.model.ChatUser;

import static ru.edu.spbstu.utils.ImageUtils.clipImageRound;

public class HboxCellWithCheckboxes extends HBox {

    public static class MyCheckBox extends CheckBox {
        protected int structNum;
        MyCheckBox()
        {
            super();
        }


    }
    ImageView  imageView;
    Label label;

    public MyCheckBox getDeleteCB() {
        return deleteCB;
    }

    public MyCheckBox getAdminCB() {
        return adminCB;
    }

    MyCheckBox deleteCB;
    MyCheckBox adminCB;

    public HboxCellWithCheckboxes()
    {
        super();
        imageView=new ImageView();
        label = new Label();
        deleteCB=new MyCheckBox();
        adminCB=new MyCheckBox();
    }

    HboxCellWithCheckboxes(String labelText, int number,Image image) {
        this();
        label.setText(labelText);
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);

        deleteCB.structNum=number;
        adminCB.structNum=number;

        imageView.setImage(image);
        imageView.setFitHeight(40);
        imageView.setFitWidth(40);
        clipImageRound(imageView);
        label.setAlignment(Pos.CENTER_LEFT);
        deleteCB.setAlignment(Pos.CENTER_RIGHT);
        adminCB.setAlignment(Pos.CENTER_RIGHT);
        HboxCellWithCheckboxes.setMargin(imageView,new Insets(5,0,5,0));
        HboxCellWithCheckboxes.setMargin(label,new Insets(0,0,0,20));
        HboxCellWithCheckboxes.setMargin(deleteCB,new Insets(0,120,0,0));
        HboxCellWithCheckboxes.setMargin(adminCB,new Insets(0,40,0,0));
        this.getChildren().addAll( imageView,label,deleteCB,adminCB);
        this.setAlignment(Pos.CENTER_LEFT);

    }
    public String getLabelText()
    {
        return label.getText();
    }
}
