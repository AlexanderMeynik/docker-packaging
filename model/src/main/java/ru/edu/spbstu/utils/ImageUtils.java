package ru.edu.spbstu.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Rectangle;

public class ImageUtils {
    public static Image imageCropper(Image image_to_Crop)
    {
        //can be used later
        PixelReader pixelReader = image_to_Crop.getPixelReader();
        if(image_to_Crop.getHeight()== image_to_Crop.getWidth())
        {
            return image_to_Crop;
        }
        double size= Math.min(image_to_Crop.getHeight(), image_to_Crop.getWidth());
        return new WritableImage(pixelReader, 0, 0, (int)size, (int)size);
    }

    public static void clipImageRound(ImageView pictureImageView) {
        final Rectangle clip = new Rectangle();
        clip.arcWidthProperty().bind(clip.heightProperty().divide(0.1));
        clip.arcHeightProperty().bind(clip.heightProperty().divide(0.1));
        clip.setWidth( pictureImageView.getLayoutBounds().getWidth());
        clip.setHeight( pictureImageView.getLayoutBounds().getHeight());
        pictureImageView.setClip(clip);
    }
}
