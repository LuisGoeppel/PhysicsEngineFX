package Controller;

import Basics.*;
import Controls.Gravity;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class EngineController {

    @FXML
    private Button btn_triangle;
    @FXML
    private Button btn_circle;
    @FXML
    private Button btn_square;
    @FXML
    private Button btn_rotate_box;
    @FXML
    private TextField field_X, field_Y, field_XRotRect, field_YRotRect;
    @FXML
    private TextField field_width, field_height, field_widthRotRect, field_heightRotRect;
    @FXML
    private TextField fieldAX, fieldAY, fieldBX, fieldBY, fieldCX, fieldCY;
    @FXML
    private Label label_width, label_height;
    @FXML
    private TextField field_angleRotRect;
    @FXML
    private Label label_simulate;
    @FXML
    private CheckBox box_gravity, box_gravityTri, box_gravityRotRect;
    @FXML
    private CheckBox box_collidable, box_collidableTri, box_collidableRotRect;
    @FXML
    private AnchorPane gamePane;
    @FXML
    private AnchorPane propertyPane, propertyPaneTri, propertyPaneRotBox;
    private List<PhysicsObject> objects;
    private MovableObjectType selected;
    private final double defaultSize = 70;
    private double currentSize;
    private final double minSize = 10;
    private final double maxSize = 400;
    private final double playerSpeed = 10;
    private final double scrollIncreaseFactor = 7;
    private int maxObjects = 15;
    private int selectedElementIndex, playerElementIndex;
    private Rectangle selectedRect;
    private Circle selectedCircle;
    private Polygon selectedPoly;
    private Vec2D currentMousePos;
    private Timer timer;
    private TimerTask task;
    private Gravity gravity;
    private boolean simulate;
    private String btnBackgroundColor = "#dedede";
    private String selectedColor = "LightGreen";
    private Color transparent = new Color(0, 0, 0, 0);

    public void init() {
        System.out.println("Initializing");
        setBtnStyleDefault();
        gravity = new Gravity(604);

        selected = null;
        simulate = false;
        objects = new ArrayList<>();
        selectedElementIndex = -1;
        playerElementIndex = -1;
        currentSize = defaultSize;
        currentMousePos = new Vec2D(0, 0);
        setPropertyPanesInvisible();
        initSelectionElements();

        gamePane.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {

            modifySelectedElement();
            if (selected != null && objects.size() < maxObjects) {
                double x = mouseEvent.getX();
                double y = mouseEvent.getY();
                switch (selected) {
                    case BOX:
                        Vec2D bottomLeftBox = new Vec2D(x - currentSize/2, y - currentSize/2);
                        Box2D newBox = new Box2D(bottomLeftBox, currentSize, currentSize);
                        if (!collidesWithOtherObjects(newBox) && curShapeInsideGameRect()) {
                            PhysicsObject object = new PhysicsObject(newBox, null, false, true);
                            objects.add(object);
                            gravity.add(object);
                        }
                        break;
                    case CIRCLE:
                        Vec2D center = new Vec2D(x, y);
                        Circle2D newCircle2D = new Circle2D(center, currentSize/2);
                        if (!collidesWithOtherObjects(newCircle2D) && curShapeInsideGameRect()) {
                            PhysicsObject object = new PhysicsObject(newCircle2D, null, false, true);
                            objects.add(object);
                            gravity.add(object);
                        }
                        break;
                    case TRIANGLE:
                        Vec2D bottomLeftTri = new Vec2D(x - currentSize/2, y - currentSize/2);
                        Vec2D bottomRightTri = new Vec2D(x + currentSize/2, y - currentSize/2);
                        Vec2D top = new Vec2D(x, y + currentSize/2);
                        Triangle2D newTri = new Triangle2D(bottomLeftTri, bottomRightTri, top);
                        if (!collidesWithOtherObjects(newTri) && curShapeInsideGameRect()) {
                            PhysicsObject object = new PhysicsObject(newTri, null, false, true);
                            objects.add(object);
                            gravity.add(object);
                        }
                        break;
                    case ROTATION_BOX:
                        RotationBox2D newBoxRot = new RotationBox2D(new Vec2D(x, y), currentSize, currentSize, 0);
                        if (!collidesWithOtherObjects(newBoxRot) && curShapeInsideGameRect()) {
                            PhysicsObject object = new PhysicsObject(newBoxRot, null, false, true);
                            objects.add(object);
                            gravity.add(object);
                        }
                        break;
                }
            } else if (selected == null) {
                boolean hasSelected = false;
                for (int i = 0; i < objects.size(); i++) {
                    if (objects.get(i).object.contains(new Vec2D(mouseEvent.getX(), mouseEvent.getY()))) {
                        selectedElementIndex = i;
                        hasSelected = true;
                        showSelectedElementOptions();
                        break;
                    }
                }
                if (!hasSelected) {
                    setPropertyPanesInvisible();
                    selectedElementIndex = -1;
                }
            }
        });

        gamePane.addEventHandler(MouseEvent.MOUSE_MOVED, mouseEvent -> {
            currentMousePos.x = mouseEvent.getX();
            currentMousePos.y = mouseEvent.getY();
        });

        //Game Ticks
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                if (simulate) {
                    gravity.tick();
                    Platform.runLater(() -> updateProperties());
                }
                Platform.runLater(() -> drawObjects());
            }
        };
        timer.schedule(task, 0, 16);
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case DELETE:
                deleteSelectedObject();
                break;
            case RIGHT:
                movePlayerRight();
                break;
            case LEFT:
                movePlayerLeft();
                break;
            case SPACE:
                playerJump();
                break;
        }
    }

    public void onScroll(ScrollEvent scrollEvent) {
        if (selected != null) {
            double deltaY = scrollEvent.getDeltaY();
            if (deltaY < 0) {
                currentSize = Math.max(minSize, currentSize - scrollIncreaseFactor);
            } else if (deltaY > 0) {
                currentSize = Math.min(maxSize, currentSize + scrollIncreaseFactor);
            }
        }
    }
    @FXML
    public void close() {
        timer.cancel();
        System.exit(0);
    }

    @FXML
    public void selectBox() {
        selected = MovableObjectType.BOX;
        selectedElementIndex = -1;
        setSelectedNotVisible();
        setBtnStyleDefault();
        btn_square.setStyle("-fx-background-color: " + selectedColor);
        setPropertyPanesInvisible();
    }

    @FXML
    public void selectCircle() {
        selected = MovableObjectType.CIRCLE;
        selectedElementIndex = -1;
        setSelectedNotVisible();
        setBtnStyleDefault();
        btn_circle.setStyle("-fx-background-color: " + selectedColor);
        setPropertyPanesInvisible();
    }

    @FXML
    public void selectTriangle() {
        selected = MovableObjectType.TRIANGLE;
        selectedElementIndex = -1;
        setSelectedNotVisible();
        setBtnStyleDefault();
        btn_triangle.setStyle("-fx-background-color: " + selectedColor);
        setPropertyPanesInvisible();
    }

    @FXML
    public void selectRotationBox() {
        selected = MovableObjectType.ROTATION_BOX;
        selectedElementIndex = -1;
        setSelectedNotVisible();
        setBtnStyleDefault();
        btn_rotate_box.setStyle("-fx-background-color: " + selectedColor);
        setPropertyPanesInvisible();
    }

    @FXML
    public void deselectButtons() {
        modifySelectedElement();
        selected = null;
        selectedElementIndex = -1;
        setSelectedNotVisible();
        setBtnStyleDefault();
        setPropertyPanesInvisible();
    }

    @FXML
    public void deleteSelectedObject() {
        if (selectedElementIndex >= 0) {
            gamePane.getChildren().remove(objects.get(selectedElementIndex).representation);
            objects.remove(selectedElementIndex);
            if (selectedElementIndex == playerElementIndex) {
                playerElementIndex = -1;
            } else if (selectedElementIndex < playerElementIndex) {
                playerElementIndex--;
            }
            selectedElementIndex = -1;
            setPropertyPanesInvisible();
        }
    }

    @FXML
    public void deleteAllObjects() {
        gamePane.getChildren().clear();
        objects.clear();
        gravity.clear();
        selectedElementIndex = -1;
        playerElementIndex = -1;
        gamePane.getChildren().add(selectedPoly);
        gamePane.getChildren().add(selectedRect);
        gamePane.getChildren().add(selectedCircle);
        setPropertyPanesInvisible();
    }

    @FXML
    public void giveAllObjectsGravity() {
        for (PhysicsObject o : objects) {
            o.hasGravity = true;
        }
    }

    @FXML
    public void startSimulation() {
        modifySelectedElement();
        label_simulate.setVisible(true);
        simulate = true;
    }

    @FXML
    public void endSimulation() {
        simulate = false;
        label_simulate.setVisible(false);
    }

    @FXML
    public void selectCurrentObjectAsPlayer() {
        playerElementIndex = selectedElementIndex;
    }

    @FXML
    public void modifySelectedElement() {
        if (selectedElementIndex != -1) {
            switch (objects.get(selectedElementIndex).object.getObjectType()) {
                case BOX:
                    Box2D currentBox = (Box2D) ((objects.get(selectedElementIndex).object));
                    Movable backupIfCollidingBox = currentBox.getCopy();
                    currentBox.setCenter(new Vec2D(Double.parseDouble(field_X.getText()),
                            Double.parseDouble(field_Y.getText())));
                    currentBox.setWidth(Double.parseDouble(field_width.getText()));
                    currentBox.setHeight(Double.parseDouble(field_height.getText()));
                    if (collidesWithOtherObjects(currentBox, selectedElementIndex)) {
                        objects.get(selectedElementIndex).object = backupIfCollidingBox;
                    }

                    objects.get(selectedElementIndex).hasGravity = box_gravity.isSelected();
                    objects.get(selectedElementIndex).isCollidable = box_collidable.isSelected();
                    break;
                case CIRCLE:
                    Circle2D currentCircle = (Circle2D) (objects.get(selectedElementIndex).object);
                    Movable backupIfCollidingCircle = currentCircle.getCopy();
                    currentCircle.setCenter(new Vec2D(Double.parseDouble(field_X.getText()),
                            Double.parseDouble(field_Y.getText())));
                    currentCircle.setRadius(Double.parseDouble(field_width.getText()));
                    if (collidesWithOtherObjects(currentCircle, selectedElementIndex)) {
                        objects.get(selectedElementIndex).object = backupIfCollidingCircle;
                    }

                    objects.get(selectedElementIndex).hasGravity = box_gravity.isSelected();
                    objects.get(selectedElementIndex).isCollidable = box_collidable.isSelected();
                    break;
                case TRIANGLE:
                    Triangle2D currentTri = (Triangle2D) (objects.get(selectedElementIndex).object);
                    Movable backupIfCollidingTri = currentTri.getCopy();
                    currentTri.setA(new Vec2D(Double.parseDouble(fieldAX.getText()),
                            Double.parseDouble(fieldAY.getText())));
                    currentTri.setB(new Vec2D(Double.parseDouble(fieldBX.getText()),
                            Double.parseDouble(fieldBY.getText())));
                    currentTri.setC(new Vec2D(Double.parseDouble(fieldCX.getText()),
                            Double.parseDouble(fieldCY.getText())));
                    if (collidesWithOtherObjects(currentTri, selectedElementIndex)) {
                        objects.get(selectedElementIndex).object = backupIfCollidingTri;
                    }

                    objects.get(selectedElementIndex).hasGravity = box_gravityTri.isSelected();
                    objects.get(selectedElementIndex).isCollidable = box_collidableTri.isSelected();
                    break;
                case ROTATION_BOX:
                    RotationBox2D currentRotBox = (RotationBox2D) (objects.get(selectedElementIndex).object);
                    Movable backupIfCollidingRotBox = currentRotBox.getCopy();
                    currentRotBox.setCenter(new Vec2D(Double.parseDouble(field_XRotRect.getText()),
                            Double.parseDouble(field_YRotRect.getText())));
                    currentRotBox.setWidth(Double.parseDouble(field_widthRotRect.getText()));
                    currentRotBox.setHeight(Double.parseDouble(field_heightRotRect.getText()));
                    currentRotBox.setRotationAngle(Double.parseDouble(field_angleRotRect.getText()));
                    if (collidesWithOtherObjects(currentRotBox, selectedElementIndex)) {
                        objects.get(selectedElementIndex).object = backupIfCollidingRotBox;
                    }

                    objects.get(selectedElementIndex).hasGravity = box_gravityRotRect.isSelected();
                    objects.get(selectedElementIndex).isCollidable = box_collidableRotRect.isSelected();
                    break;
            }

            gravity.updateElements(objects);
        }
    }

    private void setBtnStyleDefault() {
        btn_circle.setStyle("-fx-background-color: " + btnBackgroundColor);
        btn_square.setStyle("-fx-background-color: " + btnBackgroundColor);
        btn_rotate_box.setStyle("-fx-background-color: " + btnBackgroundColor);
        btn_triangle.setStyle("-fx-background-color: " + btnBackgroundColor);
    }

    private boolean collidesWithOtherObjects(Movable rhs) {

        for (PhysicsObject o : objects) {
            if (!o.equals(rhs) && Utility.isColliding(o.object, rhs)) {
                return true;
            }
        }
        return false;
    }

    private boolean collidesWithOtherObjects(Movable rhs, int indexNotToCheck) {

        for (int i = 0; i < objects.size(); i++) {
            if (i != indexNotToCheck && Utility.isColliding(objects.get(i).object, rhs)) {
                return true;
            }
        }
        return false;
    }

    private boolean curShapeInsideGameRect() {

        double gameRectWidth = gamePane.getWidth();
        double gameRectHeight = gamePane.getHeight();
        double x = currentMousePos.x;
        double y = currentMousePos.y;

        if (selected == null) {
            return true;
        }
        switch (selected) {
            case BOX:
            case CIRCLE:
            case ROTATION_BOX:
                return 0 < x - currentSize/2 && x + currentSize/2 < gameRectWidth - 5
                        && 0 < y - currentSize/2 && y + currentSize/2 < gameRectHeight - 5;
            case TRIANGLE:
                Box2D gameBox = new Box2D(new Vec2D(0,0), gameRectWidth, gameRectHeight);
                Vec2D bottomLeftTri = new Vec2D(x - currentSize/2, y - currentSize/2);
                Vec2D bottomRightTri = new Vec2D(x + currentSize/2, y - currentSize/2);
                Vec2D top = new Vec2D(x, y + currentSize/2);

                return gameBox.contains(bottomLeftTri) && gameBox.contains(bottomRightTri) && gameBox.contains(top);
            default:
                return false;
        }
    }

    private void initSelectionElements() {
        selectedRect = new Rectangle();
        selectedCircle = new Circle();
        selectedPoly = new Polygon();

        selectedCircle.setStrokeWidth(2);
        selectedCircle.setStroke(Color.GREY);
        selectedCircle.setFill(transparent);

        selectedRect.setStrokeWidth(2);
        selectedRect.setStroke(Color.GREY);
        selectedRect.setFill(transparent);

        selectedPoly.setStrokeWidth(2);
        selectedPoly.setStroke(Color.GREY);
        selectedPoly.setFill(transparent);

        setSelectedNotVisible();

        gamePane.getChildren().add(selectedCircle);
        gamePane.getChildren().add(selectedRect);
        gamePane.getChildren().add(selectedPoly);
    }

    private void showSelectedElementOptions() {
        if (selectedElementIndex != -1) {
            switch (objects.get(selectedElementIndex).object.getObjectType()) {
                case BOX:
                    setPropertyPanesInvisible();
                    initPropertiesBox();
                    propertyPane.setVisible(true);
                    box_gravity.setSelected(objects.get(selectedElementIndex).hasGravity);
                    box_collidable.setSelected(objects.get(selectedElementIndex).isCollidable);
                    break;
                case CIRCLE:
                    setPropertyPanesInvisible();
                    initPropertiesCircle();
                    propertyPane.setVisible(true);
                    box_gravity.setSelected(objects.get(selectedElementIndex).hasGravity);
                    box_collidable.setSelected(objects.get(selectedElementIndex).isCollidable);
                    break;
                case TRIANGLE:
                    setPropertyPanesInvisible();
                    initPropertiesTri();
                    propertyPaneTri.setVisible(true);
                    box_gravityTri.setSelected(objects.get(selectedElementIndex).hasGravity);
                    box_collidableTri.setSelected(objects.get(selectedElementIndex).isCollidable);
                    break;
                case ROTATION_BOX:
                    setPropertyPanesInvisible();
                    initPropertiesRotBox();
                    propertyPaneRotBox.setVisible(true);
                    box_gravityRotRect.setSelected(objects.get(selectedElementIndex).hasGravity);
                    box_collidableRotRect.setSelected(objects.get(selectedElementIndex).isCollidable);
                    break;
            }
        }
    }

    private void initPropertiesBox() {
        label_width.setText("Width:");
        label_height.setVisible(true);
        field_height.setVisible(true);

        Box2D currentBox = (Box2D) (objects.get(selectedElementIndex).object);
        field_X.setText(convertToStringOneDecimal(currentBox.getCenter().x));
        field_Y.setText(convertToStringOneDecimal(currentBox.getCenter().y));
        field_width.setText(convertToStringOneDecimal(currentBox.getWidth()));
        field_height.setText(convertToStringOneDecimal(currentBox.getHeight()));
    }

    private void initPropertiesCircle() {
        label_width.setText("Radius:");
        label_height.setVisible(false);
        field_height.setVisible(false);

        Circle2D currentCircle = (Circle2D) (objects.get(selectedElementIndex).object);
        field_X.setText(convertToStringOneDecimal(currentCircle.getCenter().x));
        field_Y.setText(convertToStringOneDecimal(currentCircle.getCenter().y));
        field_width.setText(convertToStringOneDecimal(currentCircle.getRadius()));
    }

    private void initPropertiesTri() {
        Triangle2D currentTri = (Triangle2D) (objects.get(selectedElementIndex).object);

        fieldAX.setText(convertToStringOneDecimal(currentTri.getA().x));
        fieldAY.setText(convertToStringOneDecimal(currentTri.getA().y));
        fieldBX.setText(convertToStringOneDecimal(currentTri.getB().x));
        fieldBY.setText(convertToStringOneDecimal(currentTri.getB().y));
        fieldCX.setText(convertToStringOneDecimal(currentTri.getC().x));
        fieldCY.setText(convertToStringOneDecimal(currentTri.getC().y));
    }

    private void initPropertiesRotBox() {
        RotationBox2D currentRotBox = (RotationBox2D) (objects.get(selectedElementIndex).object);

        field_XRotRect.setText(convertToStringOneDecimal(currentRotBox.getCenter().x));
        field_YRotRect.setText(convertToStringOneDecimal(currentRotBox.getCenter().y));
        field_widthRotRect.setText(convertToStringOneDecimal(currentRotBox.getWidth()));
        field_heightRotRect.setText(convertToStringOneDecimal(currentRotBox.getHeight()));
        field_angleRotRect.setText(convertToStringOneDecimal(currentRotBox.getRotationAngle()));
    }

    private void updateProperties() {
        if (selectedElementIndex != -1) {

            switch (objects.get(selectedElementIndex).object.getObjectType()) {
                case BOX:
                    Box2D currentBox = (Box2D) (objects.get(selectedElementIndex).object);

                    field_X.setText(convertToStringOneDecimal(currentBox.getCenter().x));
                    field_Y.setText(convertToStringOneDecimal(currentBox.getCenter().y));
                    field_width.setText(convertToStringOneDecimal(currentBox.getWidth()));
                    field_height.setText(convertToStringOneDecimal(currentBox.getHeight()));
                    break;
                case CIRCLE:
                    Circle2D currentCircle = (Circle2D) (objects.get(selectedElementIndex).object);

                    field_X.setText(convertToStringOneDecimal(currentCircle.getCenter().x));
                    field_Y.setText(convertToStringOneDecimal(currentCircle.getCenter().y));
                    field_width.setText(convertToStringOneDecimal(currentCircle.getRadius()));
                    break;
                case TRIANGLE:
                    initPropertiesTri();
                    break;
                case ROTATION_BOX:
                    initPropertiesRotBox();
                    break;
            }
        }
    }

    private void movePlayerRight() {
        if (playerElementIndex != -1 && simulate) {
            Movable playerObject = objects.get(playerElementIndex).object;
            if (playerObject.getRight() + playerSpeed < gamePane.getWidth()) {
                playerObject.move(new Vec2D(playerSpeed, 0));
            }
            if (collidesWithOtherObjects(playerObject, playerElementIndex) ) {
                playerObject.move(new Vec2D(-playerSpeed, 0));
            }
        }
    }

    private void movePlayerLeft() {
        if (playerElementIndex != -1 && simulate) {
            Movable playerObject = objects.get(playerElementIndex).object;
            if (playerObject.getLeft() - playerSpeed > 0) {
                playerObject.move(new Vec2D(-playerSpeed, 0));
            }
            if (collidesWithOtherObjects(playerObject, playerElementIndex) ) {
                playerObject.move(new Vec2D(playerSpeed, 0));
            }
        }
    }

    private void playerJump() {
        if (playerElementIndex != -1 && simulate) {

        }
    }

    private void setPropertyPanesInvisible() {
        propertyPane.setVisible(false);
        propertyPaneTri.setVisible(false);
        propertyPaneRotBox.setVisible(false);
    }

    private void setSelectedNotVisible() {
        selectedPoly.setVisible(false);
        selectedCircle.setVisible(false);
        selectedRect.setVisible(false);
    }

    private String convertToStringOneDecimal(Double d) {
        String doubleString = Double.toString(d);
        int index = doubleString.indexOf('.');
        return doubleString.substring(0, Math.min(doubleString.length(), index + 2));
    }

    private void drawObjects() {

        for(int i = 0; i < objects.size(); i++) {
            switch (objects.get(i).object.getObjectType()) {
                case BOX:
                    Box2D box = (Box2D)(objects.get(i).object);

                    if (objects.get(i).representation == null) {
                        Rectangle rectBox = new Rectangle();
                        rectBox.setStrokeWidth(5);
                        rectBox.setStrokeLineCap(StrokeLineCap.ROUND);
                        rectBox.setStrokeType(StrokeType.INSIDE);
                        rectBox.setStroke(Color.BLACK);
                        rectBox.setFill(transparent);
                        objects.get(i).representation = rectBox;
                        gamePane.getChildren().add(rectBox);
                    }

                    Rectangle rect = (Rectangle)(objects.get(i).representation);
                    rect.setLayoutX(box.getLeft());
                    rect.setLayoutY(box.getBottom());
                    rect.setWidth(box.getWidth());
                    rect.setHeight(box.getHeight());

                    if (playerElementIndex == i) {
                        rect.setFill(Color.DEEPSKYBLUE);
                    } else if (selectedElementIndex == i) {
                        rect.setFill(Color.LIGHTGREEN);
                    } else {
                        rect.setFill(transparent);
                    }
                    break;
                case ROTATION_BOX:
                    RotationBox2D boxRot = (RotationBox2D) (objects.get(i).object);

                    if (objects.get(i).representation == null) {
                        List<Vec2D> points = boxRot.getPoints();
                        Polygon rotBox = new Polygon(
                                points.get(0).x, points.get(0).y,
                                points.get(1).x, points.get(1).y,
                                points.get(2).x, points.get(2).y,
                                points.get(3).x, points.get(3).y
                        );
                        rotBox.setStrokeWidth(5);
                        rotBox.setStrokeLineCap(StrokeLineCap.ROUND);
                        rotBox.setStroke(Color.BLACK);
                        rotBox.setStrokeType(StrokeType.INSIDE);
                        rotBox.setFill(transparent);
                        objects.get(i).representation = rotBox;
                        gamePane.getChildren().add(rotBox);
                    }

                    Polygon rectRot = (Polygon) (objects.get(i).representation);
                    List<Vec2D> points = boxRot.getPoints();
                    for(int j = 0; j < points.size(); j++) {
                        rectRot.getPoints().set(j * 2, points.get(j).x);
                        rectRot.getPoints().set(j * 2 + 1, points.get(j).y);
                    }

                    if (playerElementIndex == i) {
                        rectRot.setFill(Color.DEEPSKYBLUE);
                    } else if (selectedElementIndex == i) {
                        rectRot.setFill(Color.LIGHTGREEN);
                    } else {
                        rectRot.setFill(transparent);
                    }
                    break;
                case CIRCLE:
                    Circle2D c = (Circle2D) (objects.get(i).object);

                    if (objects.get(i).representation == null) {
                        Circle circle = new Circle();
                        circle.setStroke(Color.BLACK);
                        circle.setFill(transparent);
                        circle.setStrokeWidth(5);
                        circle.setStrokeType(StrokeType.INSIDE);
                        objects.get(i).representation = circle;
                        gamePane.getChildren().add(circle);
                    }
                    Circle circle = (Circle)(objects.get(i).representation);
                    circle.setCenterX(c.getCenter().x);
                    circle.setCenterY(c.getCenter().y);
                    circle.setRadius(c.getRadius());

                    if (playerElementIndex == i) {
                        circle.setFill(Color.DEEPSKYBLUE);
                    } else if (selectedElementIndex == i) {
                        circle.setFill(Color.LIGHTGREEN);
                    } else {
                        circle.setFill(transparent);
                    }
                    break;
                case TRIANGLE:
                    Triangle2D triangle2D = (Triangle2D) (objects.get(i).object);

                    if (objects.get(i).representation == null) {
                        Polygon polygon = new Polygon(
                                triangle2D.getA().x, triangle2D.getA().y,
                                triangle2D.getB().x, triangle2D.getB().y,
                                triangle2D.getC().x, triangle2D.getC().y
                        );
                        polygon.setFill(transparent);
                        polygon.setStroke(Color.BLACK);
                        polygon.setStrokeType(StrokeType.INSIDE);
                        polygon.setStrokeWidth(5);
                        objects.get(i).representation = polygon;
                        gamePane.getChildren().add(polygon);
                    }
                    Polygon polygon = (Polygon) (objects.get(i).representation);
                    polygon.getPoints().set(0, triangle2D.getA().x);
                    polygon.getPoints().set(1, triangle2D.getA().y);
                    polygon.getPoints().set(2, triangle2D.getB().x);
                    polygon.getPoints().set(3, triangle2D.getB().y);
                    polygon.getPoints().set(4, triangle2D.getC().x);
                    polygon.getPoints().set(5, triangle2D.getC().y);

                    if (playerElementIndex == i) {
                        polygon.setFill(Color.DEEPSKYBLUE);
                    } else if (selectedElementIndex == i) {
                        polygon.setFill(Color.LIGHTGREEN);
                    } else {
                        polygon.setFill(transparent);
                    }
                default:
                    break;
            }
        }

        if (selected != null && curShapeInsideGameRect()) {
            double x = currentMousePos.x;
            double y = currentMousePos.y;

            switch (selected) {
                case BOX:
                case ROTATION_BOX:
                    selectedRect.setLayoutX(x - currentSize/2);
                    selectedRect.setLayoutY(y - currentSize/2);
                    selectedRect.setWidth(currentSize);
                    selectedRect.setHeight(currentSize);
                    if (!selectedRect.isVisible()) {
                        selectedRect.setVisible(true);
                    }
                    break;
                case CIRCLE:
                    selectedCircle.setCenterX(x);
                    selectedCircle.setCenterY(y);
                    selectedCircle.setRadius(currentSize/2);
                    if (!selectedCircle.isVisible()) {
                        selectedCircle.setVisible(true);
                    }
                    break;
                case TRIANGLE:
                    selectedPoly.getPoints().clear();
                    selectedPoly.getPoints().addAll(x - currentSize/2, y - currentSize/2);
                    selectedPoly.getPoints().addAll(x + currentSize/2, y - currentSize/2);
                    selectedPoly.getPoints().addAll(x , y + currentSize/2);
                    if (!selectedPoly.isVisible()) {
                        selectedPoly.setVisible(true);
                    }
                    break;
                default:
                    break;
            }
        } else {
           setSelectedNotVisible();
        }
    }
}
