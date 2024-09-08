package ru.edu.spbstu.clientComponents;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Rectangle;

import static ru.edu.spbstu.utils.ImageUtils.clipImageRound;

public class HBoxCell<T> extends HBox {
    public static class MyButton extends Button {
        protected int structNum;
        MyButton()
        {
            super();
        }
        MyButton(String buttonText,int num)
        {
            super();
            this.setText(buttonText);
            structNum=num;

        }

    }
    ImageView imageView;
    protected Label label;

    public MyButton getButton() {
        return button;
    }

    protected MyButton button;
    public HBoxCell()
    {
        super();
        imageView=new ImageView();
        button= new MyButton();
        label = new Label();

    }
    public String getLoginLabelText()
    {
        return label.getText();
    }

    HBoxCell(T labelText, String buttonText, int number, Image image) {
        this();


        imageView.setImage(image);
        imageView.setFitHeight(40);
        imageView.setFitWidth(40);
        clipImageRound(imageView);
        /*final Rectangle clip = new Rectangle();
        clip.arcWidthProperty().bind(clip.heightProperty().divide(0.1));
        clip.arcHeightProperty().bind(clip.heightProperty().divide(0.1));
        clip.setWidth( imageView.getLayoutBounds().getWidth());
        clip.setHeight( imageView.getLayoutBounds().getHeight());
        imageView.setClip(clip);*/


        label.setAlignment(Pos.CENTER_LEFT);
        button.setAlignment(Pos.CENTER_RIGHT);
        HboxCellWithCheckboxes.setMargin(label,new Insets(0,0,0,20));
        HboxCellWithCheckboxes.setMargin(imageView,new Insets(5,0,5,0));
        HboxCellWithCheckboxes.setMargin(button,new Insets(0,40,0,0));



        label.setText(labelText.toString());
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);

        button.setText(buttonText);
        button.structNum=number;

        this.getChildren().addAll(imageView,label, button);
        this.setAlignment(Pos.CENTER_LEFT);
    }
    public String getLabelText()
    {
        return label.getText();
    }
}
