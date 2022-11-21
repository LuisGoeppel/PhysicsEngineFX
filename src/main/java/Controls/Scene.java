package Controls;

import Basics.Collider;

import java.util.ArrayList;
import java.util.List;

public class Scene {

    private List<Collider> sceneObjects;

    public Scene () {
        sceneObjects = new ArrayList<>();
    }

    public void addObject (Collider object) {
        sceneObjects.add(object);
    }

    public List<Collider> getSceneObjects() {
        return sceneObjects;
    }
}
