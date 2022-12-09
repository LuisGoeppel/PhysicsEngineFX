package Controller;

import Basics.*;
import Controls.Gravity;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

@SuppressWarnings("FieldCanBeLocal")
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
    private Slider slider_bounciness, slider_bouncinessTri;
    @FXML
    private AnchorPane gamePane;
    @FXML
    private AnchorPane propertyPane, propertyPaneTri, propertyPaneRotBox;
    private List<PhysicsObject> objects;
    private MovableObjectType selected;
    private double currentSize;
    private final double defaultSize = 70;
    private final double minSize = 10;
    private final double maxSize = 400;
    private final double playerSpeed = 7;
    private final int playerJumpStrength = 54;
    private final double scrollIncreaseFactor = 7;
    private final int maxObjects = 25;
    private int currentID = 1;
    private final boolean defaultGravity = false;
    private final boolean defaultCollidable = true;
    private int selectedElementIndex, playerElementIndex;
    private Rectangle selectionRect;
    private Circle selectionCircle;
    private Polygon selectionPoly;
    private Vec2D currentMousePos;
    private boolean mousePosChanged, sizeChanged;
    private Timer timer;
    private TimerTask task;
    private Gravity gravity;
    private boolean simulate;
    private final String btnBackgroundColor = "#dedede";
    private final String selectedColor = "LightGreen";
    private final Color transparent = new Color(0, 0, 0, 0);
    private final int maxFPS = 60;

    public void init() {
        System.out.println("Initializing");
        setBtnStyleDefault();
        gravity = new Gravity(maxFPS);

        selected = null;
        simulate = false;
        objects = new ArrayList<>();
        selectedElementIndex = -1;
        playerElementIndex = -1;
        currentSize = defaultSize;
        currentMousePos = new Vec2D(0, 0);
        mousePosChanged = false;
        sizeChanged = false;
        setPropertyPanesInvisible();
        initSelectionElements();

        gamePane.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {

            modifySelectedElement();
            if (selected != null && objects.size() < maxObjects) {
                double x = mouseEvent.getX();
                double y = swapY(mouseEvent.getY());
                switch (selected) {
                    case BOX:
                        Vec2D bottomLeftBox = new Vec2D(x - currentSize/2, y - currentSize/2);
                        Box2D newBox = new Box2D(bottomLeftBox, currentSize, currentSize);
                        if (!collidesWithOtherObjects(newBox) && curShapeInsideGameRect()) {
                            PhysicsObject object = new PhysicsObject(
                                    newBox, null, defaultGravity, defaultCollidable, 0, currentID);
                            objects.add(object);
                            gravity.add(object);
                            currentID++;
                        }
                        break;
                    case CIRCLE:
                        Vec2D center = new Vec2D(x, y);
                        Circle2D newCircle2D = new Circle2D(center, currentSize/2);
                        if (!collidesWithOtherObjects(newCircle2D) && curShapeInsideGameRect()) {
                            PhysicsObject object = new PhysicsObject(
                                    newCircle2D, null, defaultGravity, defaultCollidable, 0, currentID);
                            objects.add(object);
                            gravity.add(object);
                            currentID++;
                        }
                        break;
                    case TRIANGLE:
                        Vec2D bottomLeftTri = new Vec2D(x - currentSize/2, y - currentSize/2);
                        Vec2D bottomRightTri = new Vec2D(x + currentSize/2, y - currentSize/2);
                        Vec2D top = new Vec2D(x, y + currentSize/2);
                        Triangle2D newTri = new Triangle2D(bottomLeftTri, bottomRightTri, top);
                        if (!collidesWithOtherObjects(newTri) && curShapeInsideGameRect()) {
                            PhysicsObject object = new PhysicsObject(
                                    newTri, null, defaultGravity, defaultCollidable, 0, currentID);
                            objects.add(object);
                            gravity.add(object);
                            currentID++;
                        }
                        break;
                    case ROTATION_BOX:
                        RotationBox2D newBoxRot = new RotationBox2D(new Vec2D(x, y), currentSize, currentSize, 0);
                        if (!collidesWithOtherObjects(newBoxRot) && curShapeInsideGameRect()) {
                            PhysicsObject object = new PhysicsObject(
                                    newBoxRot, null, defaultGravity, defaultCollidable, 0, currentID);
                            objects.add(object);
                            gravity.add(object);
                            currentID++;
                        }
                        break;
                }
            } else if (selected == null) {

                boolean hasSelected = false;
                for (int i = 0; i < objects.size(); i++) {
                    Vec2D currentMousePos = new Vec2D(mouseEvent.getX(), swapY(mouseEvent.getY()));
                    if (objects.get(i).object.contains(currentMousePos)) {
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
            mousePosChanged = true;
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
                Platform.runLater(() -> mousePosChanged = false);
            }
        };
        timer.schedule(task, 0, 1000 / maxFPS);
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case DELETE:
                deleteSelectedObject();
                break;
            case RIGHT:
                gravity.movePlayerRight(playerSpeed);
                break;
            case LEFT:
                gravity.movePlayerLeft(playerSpeed);
                break;
            case SPACE:
                gravity.playerJump(playerJumpStrength);
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
        sizeChanged = true;
    }
    @FXML
    public void close() {
        timer.cancel();
        System.exit(0);
    }

    @FXML
    public void selectBox() {
        selected = MovableObjectType.BOX;
        selectObjectTasks();
        btn_square.setStyle("-fx-background-color: " + selectedColor);
    }

    @FXML
    public void selectCircle() {
        selected = MovableObjectType.CIRCLE;
        selectObjectTasks();
        btn_circle.setStyle("-fx-background-color: " + selectedColor);
    }

    @FXML
    public void selectTriangle() {
        selected = MovableObjectType.TRIANGLE;
        selectObjectTasks();
        btn_triangle.setStyle("-fx-background-color: " + selectedColor);
    }

    @FXML
    public void selectRotationBox() {
        selected = MovableObjectType.ROTATION_BOX;
        selectObjectTasks();
        btn_rotate_box.setStyle("-fx-background-color: " + selectedColor);
    }

    @FXML
    public void deselectButtons() {
        modifySelectedElement();
        selected = null;
        selectObjectTasks();
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
            gravity.updateElements(objects);
        }
    }

    @FXML
    public void deleteAllObjects() {
        gamePane.getChildren().clear();
        objects.clear();
        gravity.clear();
        selectedElementIndex = -1;
        playerElementIndex = -1;
        gamePane.getChildren().add(selectionPoly);
        gamePane.getChildren().add(selectionRect);
        gamePane.getChildren().add(selectionCircle);
        setPropertyPanesInvisible();
    }

    @FXML
    public void giveAllObjectsGravity() {
        for (int i = 0; i < objects.size(); i++) {
            objects.get(i).hasGravity = true;
            gravity.giveGravityAt(i);
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
        if (selectedElementIndex != -1) {
            playerElementIndex = selectedElementIndex;
            gravity.setPlayer(objects.get(selectedElementIndex).object);
        }
    }

    @FXML
    public void modifySelectedElement() {
        if (selectedElementIndex != -1) {

            switch (objects.get(selectedElementIndex).object.getObjectType()) {
                case BOX:
                case CIRCLE:
                    if (!simulate || !objects.get(selectedElementIndex).hasGravity) {
                        if (objects.get(selectedElementIndex).object.getObjectType() == MovableObjectType.BOX) {
                            modifyBox();
                        } else {
                            modifyCircle();
                        }
                    }
                    objects.get(selectedElementIndex).bounciness = slider_bounciness.getValue();
                    objects.get(selectedElementIndex).hasGravity = box_gravity.isSelected();
                    objects.get(selectedElementIndex).isCollidable = box_collidable.isSelected();
                    break;
                case TRIANGLE:
                    if (!simulate || !objects.get(selectedElementIndex).hasGravity) {
                        modifyTri();
                    }
                    objects.get(selectedElementIndex).bounciness = slider_bouncinessTri.getValue();
                    objects.get(selectedElementIndex).hasGravity = box_gravityTri.isSelected();
                    objects.get(selectedElementIndex).isCollidable = box_collidableTri.isSelected();
                    break;
                case ROTATION_BOX:
                    if (!simulate || !objects.get(selectedElementIndex).hasGravity) {
                        modifyRotBox();
                    }
                    objects.get(selectedElementIndex).hasGravity = box_gravityRotRect.isSelected();
                    objects.get(selectedElementIndex).isCollidable = box_collidableRotRect.isSelected();
                    break;
            }

            gravity.updateElements(objects);
            objects.get(selectedElementIndex).hasChangedPosition = true;
            objects.get(selectedElementIndex).hasChangedSize = true;
        }
    }

    private void modifyBox() {
        Box2D currentBox = (Box2D) ((objects.get(selectedElementIndex).object));
        Collider backupIfCollidingBox = currentBox.getCopy();
        currentBox.setCenter(new Vec2D(Double.parseDouble(field_X.getText()),
                Double.parseDouble(field_Y.getText())));
        currentBox.setWidth(Double.parseDouble(field_width.getText()));
        currentBox.setHeight(Double.parseDouble(field_height.getText()));
        if (collidesWithOtherObjects(currentBox, selectedElementIndex) || !shapeInsideGameRect(currentBox)) {
            objects.get(selectedElementIndex).object = backupIfCollidingBox;
        }
    }

    private void modifyCircle() {
        Circle2D currentCircle = (Circle2D) (objects.get(selectedElementIndex).object);
        Collider backupIfCollidingCircle = currentCircle.getCopy();
        currentCircle.setCenter(new Vec2D(Double.parseDouble(field_X.getText()),
                Double.parseDouble(field_Y.getText())));
        currentCircle.setRadius(Double.parseDouble(field_width.getText()));
        if (collidesWithOtherObjects(currentCircle, selectedElementIndex) || !shapeInsideGameRect(currentCircle)) {
            objects.get(selectedElementIndex).object = backupIfCollidingCircle;
        }
    }

    private void modifyTri() {
        Triangle2D currentTri = (Triangle2D) (objects.get(selectedElementIndex).object);
        Collider backupIfCollidingTri = currentTri.getCopy();
        currentTri.setA(new Vec2D(Double.parseDouble(fieldAX.getText()),
                Double.parseDouble(fieldAY.getText())));
        currentTri.setB(new Vec2D(Double.parseDouble(fieldBX.getText()),
                Double.parseDouble(fieldBY.getText())));
        currentTri.setC(new Vec2D(Double.parseDouble(fieldCX.getText()),
                Double.parseDouble(fieldCY.getText())));
        if (collidesWithOtherObjects(currentTri, selectedElementIndex) || !shapeInsideGameRect(currentTri)) {
            objects.get(selectedElementIndex).object = backupIfCollidingTri;
        }
    }

    private void modifyRotBox() {
        RotationBox2D currentRotBox = (RotationBox2D) (objects.get(selectedElementIndex).object);
        Collider backupIfCollidingRotBox = currentRotBox.getCopy();
        currentRotBox.setCenter(new Vec2D(Double.parseDouble(field_XRotRect.getText()),
                Double.parseDouble(field_YRotRect.getText())));
        currentRotBox.setWidth(Double.parseDouble(field_widthRotRect.getText()));
        currentRotBox.setHeight(Double.parseDouble(field_heightRotRect.getText()));
        currentRotBox.setRotationAngle(Double.parseDouble(field_angleRotRect.getText()));
        if (collidesWithOtherObjects(currentRotBox, selectedElementIndex) || !shapeInsideGameRect(currentRotBox)) {
            objects.get(selectedElementIndex).object = backupIfCollidingRotBox;
        }
    }

    private void setBtnStyleDefault() {
        btn_circle.setStyle("-fx-background-color: " + btnBackgroundColor);
        btn_square.setStyle("-fx-background-color: " + btnBackgroundColor);
        btn_rotate_box.setStyle("-fx-background-color: " + btnBackgroundColor);
        btn_triangle.setStyle("-fx-background-color: " + btnBackgroundColor);
    }

    private void selectObjectTasks() {
        selectedElementIndex = -1;
        setSelectedNotVisible();
        setBtnStyleDefault();
        setPropertyPanesInvisible();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean collidesWithOtherObjects(Collider rhs) {

        for (PhysicsObject o : objects) {
            if (o.isCollidable && Utility.isColliding(o.object, rhs)) {
                return true;
            }
        }
        return false;
    }

    private boolean collidesWithOtherObjects(Collider rhs, int elementIndex) {

        if (!objects.get(elementIndex).isCollidable) {
            return false;
        }
        for (int i = 0; i < objects.size(); i++) {
            if (i != elementIndex && objects.get(i).isCollidable &&
                    Utility.isColliding(objects.get(i).object, rhs)) {
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean shapeInsideGameRect(Collider shape) {
        switch (shape.getObjectType()) {
            case CIRCLE:
                Circle2D shapeCircle = (Circle2D)(shape);
                return 0 < shapeCircle.getCenter().x - shapeCircle.getRadius() && shapeCircle.getCenter().x +
                        shapeCircle.getRadius() < gamePane.getWidth() - 5 && 0 < shapeCircle.getCenter().y
                        - shapeCircle.getRadius() && shapeCircle.getCenter().y + shapeCircle.getRadius() < gamePane.getHeight() - 5;
            case TRIANGLE:
            case ROTATION_BOX:
                Polygon2D shapePolygon = (Polygon2D)(shape);
                Collider gameRect = new Box2D(new Vec2D(0, 0), gamePane.getWidth(), gamePane.getHeight());
                for (Vec2D point : shapePolygon.getPoints()) {
                    if (!gameRect.contains(point)) {
                        return false;
                    }
                }
                return true;
            default:
                return true;
        }
    }

    private void initSelectionElements() {
        selectionRect = new Rectangle();
        selectionCircle = new Circle();
        selectionPoly = new Polygon();

        selectionCircle.setStrokeWidth(2);
        selectionCircle.setStroke(Color.GREY);
        selectionCircle.setFill(transparent);

        selectionRect.setStrokeWidth(2);
        selectionRect.setStroke(Color.GREY);
        selectionRect.setFill(transparent);

        selectionPoly.setStrokeWidth(2);
        selectionPoly.setStroke(Color.GREY);
        selectionPoly.setFill(transparent);

        setSelectedNotVisible();

        gamePane.getChildren().add(selectionCircle);
        gamePane.getChildren().add(selectionRect);
        gamePane.getChildren().add(selectionPoly);
    }

    private void showSelectedElementOptions() {
        if (selectedElementIndex != -1) {
            setPropertyPanesInvisible();

            switch (objects.get(selectedElementIndex).object.getObjectType()) {
                case BOX:
                    initPropertiesBox();
                    propertyPane.setVisible(true);
                    slider_bounciness.setValue(objects.get(selectedElementIndex).bounciness);
                    box_gravity.setSelected(objects.get(selectedElementIndex).hasGravity);
                    box_collidable.setSelected(objects.get(selectedElementIndex).isCollidable);
                    break;
                case CIRCLE:
                    initPropertiesCircle();
                    propertyPane.setVisible(true);
                    slider_bounciness.setValue(objects.get(selectedElementIndex).bounciness);
                    box_gravity.setSelected(objects.get(selectedElementIndex).hasGravity);
                    box_collidable.setSelected(objects.get(selectedElementIndex).isCollidable);
                    break;
                case TRIANGLE:
                    initPropertiesTri();
                    propertyPaneTri.setVisible(true);
                    slider_bouncinessTri.setValue(objects.get(selectedElementIndex).bounciness);
                    box_gravityTri.setSelected(objects.get(selectedElementIndex).hasGravity);
                    box_collidableTri.setSelected(objects.get(selectedElementIndex).isCollidable);
                    break;
                case ROTATION_BOX:
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
        field_angleRotRect.setText(convertToStringOneDecimal(currentRotBox.getRotationAngleDeg()));
    }

    private void updateProperties() {
        if (selectedElementIndex != -1 && objects.get(selectedElementIndex).hasChangedPosition) {

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


    private void setPropertyPanesInvisible() {
        propertyPane.setVisible(false);
        propertyPaneTri.setVisible(false);
        propertyPaneRotBox.setVisible(false);
    }

    private void setSelectedNotVisible() {
        selectionPoly.setVisible(false);
        selectionCircle.setVisible(false);
        selectionRect.setVisible(false);
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
                        initShape(rectBox, i);
                    }
                    if (objects.get(i).hasChangedPosition) {
                        Rectangle rect = (Rectangle)(objects.get(i).representation);
                        rect.setLayoutX(box.getLeft());
                        rect.setLayoutY(swapY(box.getTop()));

                        objects.get(i).hasChangedPosition = false;
                    }
                    if (objects.get(i).hasChangedSize) {
                        Rectangle rect = (Rectangle)(objects.get(i).representation);
                        rect.setWidth(box.getWidth());
                        rect.setHeight(box.getHeight());

                        objects.get(i).hasChangedSize = false;
                    }
                    break;
                case ROTATION_BOX:
                    RotationBox2D boxRot = (RotationBox2D) (objects.get(i).object);

                    if (objects.get(i).representation == null) {
                        List<Vec2D> points = boxRot.getPoints();
                        Polygon rotBox = new Polygon(
                                points.get(0).x, swapY(points.get(0).y),
                                points.get(1).x, swapY(points.get(1).y),
                                points.get(2).x, swapY(points.get(2).y),
                                points.get(3).x, swapY(points.get(3).y)
                        );
                        initShape(rotBox, i);
                    }
                    if (objects.get(i).hasChangedPosition) {
                        Polygon rectRot = (Polygon) (objects.get(i).representation);
                        List<Vec2D> points = boxRot.getPoints();
                        for(int j = 0; j < points.size(); j++) {
                            rectRot.getPoints().set(j * 2, points.get(j).x);
                            rectRot.getPoints().set(j * 2 + 1, swapY(points.get(j).y));
                        }

                        objects.get(i).hasChangedPosition = false;
                    }
                    break;
                case CIRCLE:
                    Circle2D c = (Circle2D) (objects.get(i).object);

                    if (objects.get(i).representation == null) {
                        Circle circle = new Circle();
                        initShape(circle, i);
                    }
                    if (objects.get(i).hasChangedPosition) {
                        Circle circle = (Circle)(objects.get(i).representation);
                        circle.setCenterX(c.getCenter().x);
                        circle.setCenterY(swapY(c.getCenter().y));

                        objects.get(i).hasChangedPosition = false;
                    }
                    if (objects.get(i).hasChangedSize) {
                        Circle circle = (Circle)(objects.get(i).representation);
                        circle.setRadius(c.getRadius());

                        objects.get(i).hasChangedSize = false;
                    }
                    break;
                case TRIANGLE:
                    Triangle2D triangle2D = (Triangle2D) (objects.get(i).object);

                    if (objects.get(i).representation == null) {
                        Polygon polygon = new Polygon(
                                triangle2D.getA().x, swapY(triangle2D.getA().y),
                                triangle2D.getB().x, swapY(triangle2D.getB().y),
                                triangle2D.getC().x, swapY(triangle2D.getC().y)
                        );
                        initShape(polygon, i);
                    }
                    if (objects.get(i).hasChangedPosition) {
                        Polygon polygon = (Polygon) (objects.get(i).representation);
                        polygon.getPoints().set(0, triangle2D.getA().x);
                        polygon.getPoints().set(1, swapY(triangle2D.getA().y));
                        polygon.getPoints().set(2, triangle2D.getB().x);
                        polygon.getPoints().set(3, swapY(triangle2D.getB().y));
                        polygon.getPoints().set(4, triangle2D.getC().x);
                        polygon.getPoints().set(5, swapY(triangle2D.getC().y));

                        objects.get(i).hasChangedPosition = false;
                    }
                default:
                    break;
            }
            Shape currentShape = objects.get(i).representation;
            if (playerElementIndex == i) {
                currentShape.setFill(Color.DEEPSKYBLUE);
            } else if (selectedElementIndex == i) {
                currentShape.setFill(Color.LIGHTGREEN);
            } else {
                currentShape.setFill(transparent);
            }
        }

        if (selected != null && curShapeInsideGameRect()) {
            if (mousePosChanged || sizeChanged) {
                double x = currentMousePos.x;
                double y = currentMousePos.y;

                switch (selected) {
                    case BOX:
                    case ROTATION_BOX:
                        selectionRect.setLayoutX(x - currentSize/2);
                        selectionRect.setLayoutY(y - currentSize/2);
                        selectionRect.setWidth(currentSize);
                        selectionRect.setHeight(currentSize);
                        if (!selectionRect.isVisible()) {
                            selectionRect.setVisible(true);
                        }
                        break;
                    case CIRCLE:
                        selectionCircle.setCenterX(x);
                        selectionCircle.setCenterY(y);
                        selectionCircle.setRadius(currentSize/2);
                        if (!selectionCircle.isVisible()) {
                            selectionCircle.setVisible(true);
                        }
                        break;
                    case TRIANGLE:
                        selectionPoly.getPoints().clear();
                        selectionPoly.getPoints().addAll(x - currentSize/2, y + currentSize/2);
                        selectionPoly.getPoints().addAll(x + currentSize/2, y + currentSize/2);
                        selectionPoly.getPoints().addAll(x , y - currentSize/2);
                        if (!selectionPoly.isVisible()) {
                            selectionPoly.setVisible(true);
                        }
                        break;
                    default:
                        break;
                }
                sizeChanged = false;
            }
        } else {
           setSelectedNotVisible();
        }
    }

    private void initShape(Shape shape, int index) {
        shape.setFill(transparent);
        shape.setStroke(Color.BLACK);
        shape.setStrokeType(StrokeType.INSIDE);
        shape.setStrokeWidth(5);
        objects.get(index).representation = shape;
        gamePane.getChildren().add(shape);
    }

    private double swapY(double y) {
        return 604 - y;
    }

}
