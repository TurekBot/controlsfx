package org.controlsfx.dialog;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

class LightweightDialog extends FXDialog {

    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private final Scene scene;
    private Region opaqueLayer;
    private Pane dialogStack;
    private Parent originalParent;
    
    private BooleanProperty focused;
    private StringProperty title;
    private BooleanProperty resizable;
    
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    LightweightDialog(String title, Scene scene) {
        super();
        
        this.scene = scene;
        
        // *** The rest is for adding window decorations ***
        init(title);

        // add window dragging
        toolBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                mouseDragDeltaX = lightweightDialog.getLayoutX() - event.getSceneX();
                mouseDragDeltaY = lightweightDialog.getLayoutY() - event.getSceneY();
            }
        });
        toolBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                lightweightDialog.setLayoutX(event.getSceneX() + mouseDragDeltaX);
                lightweightDialog.setLayoutY(event.getSceneY() + mouseDragDeltaY);
            }
        });

        // we don't support maximising or minimising lightweight dialogs, so we 
        // remove these from the toolbar
        minButton = null;
        maxButton = null;
        windowBtns.getChildren().setAll(closeButton);
        
        // add window resizing
        EventHandler<MouseEvent> resizeHandler = new EventHandler<MouseEvent>() {
            private double width;
            private double height;
            private Point2D dragAnchor;

            @Override public void handle(MouseEvent event) {
                EventType<? extends MouseEvent> type = event.getEventType();
                
                if (type == MouseEvent.MOUSE_PRESSED) {
                    width = lightweightDialog.getWidth();
                    height = lightweightDialog.getHeight();
                    dragAnchor = new Point2D(event.getSceneX(), event.getSceneY());
                } else if (type == MouseEvent.MOUSE_DRAGGED) {
                    lightweightDialog.setPrefWidth(Math.max(lightweightDialog.minWidth(-1),   width  + (event.getSceneX() - dragAnchor.getX())));
                    lightweightDialog.setPrefHeight(Math.max(lightweightDialog.minHeight(-1), height + (event.getSceneY() - dragAnchor.getY())));
                }
            }
        };
        resizeCorner.setOnMousePressed(resizeHandler);
        resizeCorner.setOnMouseDragged(resizeHandler);
       
        // make focused by default
        lightweightDialog.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        lightweightDialog.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, true);
    }
    
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    @Override public StringProperty titleProperty() {
        if (title == null) {
            title = new SimpleStringProperty(this, "title");
        }
        return title;
    }
    
    @Override public void show() {
        // opaque layer
        opaqueLayer = new Region();
        opaqueLayer.setStyle("-fx-background-color: #00000044");
        
        // install CSS
        scene.getStylesheets().addAll(DIALOGS_CSS_URL.toExternalForm());
        
        // modify scene root to install opaque layer and the dialog
        originalParent = scene.getRoot();
        dialogStack = new Pane(originalParent, opaqueLayer, lightweightDialog) {
            protected void layoutChildren() {
                final double w = dialogStack.getWidth();
                final double h = dialogStack.getHeight();
                opaqueLayer.resizeRelocate(0, 0, w, h);
                
                final double dialogWidth = lightweightDialog.prefWidth(-1);
                final double dialogHeight = lightweightDialog.prefHeight(-1);
                
                double dialogX = lightweightDialog.getLayoutX();
                dialogX = dialogX == 0.0 ? w/2.0-dialogWidth/2.0 : dialogX;
                
                double dialogY = lightweightDialog.getLayoutY();
                dialogY = dialogY == 0.0 ? h/2.0-dialogHeight/2.0 : dialogY;
                
                lightweightDialog.relocate(dialogX, dialogY);
                lightweightDialog.resize(dialogWidth, dialogHeight);
            }
        };
        lightweightDialog.setVisible(true);
        scene.setRoot(dialogStack);
    }
    
    @Override public void hide() {
        opaqueLayer.setVisible(false);
        lightweightDialog.setVisible(false);
        
        dialogStack.getChildren().remove(originalParent);
        originalParent.getStyleClass().remove("root");
        
        scene.setRoot(originalParent);
    }

    @Override BooleanProperty resizableProperty() {
        if (resizable == null) {
            resizable = new SimpleBooleanProperty(this, "resizable", false);
        }
        return resizable;
    }

    @Override public Node getRoot() {
        return lightweightDialog;
    }

    @Override ReadOnlyDoubleProperty widthProperty() {
        return lightweightDialog.widthProperty();
    }

    @Override ReadOnlyDoubleProperty heightProperty() {
        return lightweightDialog.heightProperty();
    }    
    
    @Override BooleanProperty focusedProperty() {
        if (focused == null) {
            focused = new SimpleBooleanProperty(this, "focused", true);
        }
        return focused;
    }
    
    @Override public void setContentPane(Pane pane) {
        root.setCenter(pane);        
    }
    
    @Override public void sizeToScene() {
        // no-op: This isn't needed when there is not stage...
    }
    
    @Override void setIconified(boolean iconified) {
        // no-op: We don't want to iconify lightweight dialogs
    }
    
    @Override boolean isIconified() {
        return false;
    }
    
    @Override public void setIconifiable(boolean iconifiable) {
        // no-op: We don't want to iconify lightweight dialogs
    }
    
    @Override public void setClosable( boolean closable ) {
        closeButton.setVisible( closable );
    }
}
