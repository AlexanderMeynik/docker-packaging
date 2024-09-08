package ru.edu.spbstu.clientComponents;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import ru.edu.spbstu.model.ChatUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListViewWithCheckBoxes extends ListView<HboxCellWithCheckboxes> {
    public ListViewWithCheckBoxes()
    {
        super();
    }

    private final EventHandler<ActionEvent> handler= arg0 -> {
        var source= ((HboxCellWithCheckboxes.MyCheckBox)arg0.getSource());
        int i1 =source.structNum;

        update(i1,source);
    };
    private List<ChatUser> lst= new ArrayList<>(0);
    public List<ChatUser> getUsers()
    {
        return new ArrayList<>(lst);
    }
    public void resetList(List<ChatUser> arr,List<Image> avatars,boolean type)
    {
        var prev=this.getItems();
        this.setFixedCellSize(50);
        this.lst=arr;
        List<HboxCellWithCheckboxes> list = new ArrayList<>();
        for ( int i=0;i<lst.size();i++)
        {

            ChatUser current_item=lst.get(i);
            HboxCellWithCheckboxes temp=new HboxCellWithCheckboxes(current_item.getLogin(),i,avatars.get(i));

            temp.adminCB.setOnAction(handler);
            temp.deleteCB.setOnAction(handler);
            if(current_item.getIs_admin())
            {
                temp.adminCB.setDisable(true);
                temp.deleteCB.setDisable(true);
            }
            if(!type) {
                for (int j = 0; j < prev.size(); j++) {
                    if (prev.get(j).label.getText().equals(current_item.getLogin())) {
                        temp = prev.get(j);
                    }
                }
            }
            list.add(temp);
        }

        this.setItems(FXCollections.observableList(list));
    }

    public List<String> getUsersToDelete()
    {
        List<String> res=new ArrayList<>();
        var oblist=this.getItems();
        for (var elem:oblist)
        {
            if(elem.deleteCB.isSelected())
            {
                res.add(elem.getLabelText());
            }
        }
        return res;
    }
    public List<String> getUsersToMakeAdmins()
    {
        List<String> res=new ArrayList<>();
        var oblist=this.getItems();
        for (var elem:oblist)
        {
            if(elem.adminCB.isSelected())
            {
                res.add(elem.getLabelText());
            }
        }
        return res;
    }
   private void  update(int index, HboxCellWithCheckboxes.MyCheckBox source)
   {
       ObservableList<HboxCellWithCheckboxes> myObservableList = FXCollections.observableList(this.getItems());
       var currentElement=myObservableList.get(index);
       if(currentElement.adminCB==source)
       {
           if(source.isSelected())
           {
               currentElement.deleteCB.setSelected(false);
           }
           currentElement.deleteCB.setDisable(source.isSelected());
       }
       else
       {
           if(source.isSelected())
           {
               currentElement.adminCB.setSelected(false);
           }
           currentElement.adminCB.setDisable(source.isSelected());
       }
       myObservableList.set(index,currentElement);
       this.setItems(myObservableList);
   }
}
