package storage;

import model.Link;

import java.util.Map;

public interface LinkStorage {

    Map<String, Link> loadAllLinks();

    void saveLinks(Map<String, Link> links);
}
