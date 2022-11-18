package Controls;

import Basics.Movable;

import java.util.ArrayList;
import java.util.List;

public class Scene {

    private List<Movable> sceneObjects;

    public Scene () {
        sceneObjects = new ArrayList<>();
    }

    public void addObject (Movable object) {
        sceneObjects.add(object);
    }

    public List<Movable> getSceneObjects() {
        return sceneObjects;
    }
}
