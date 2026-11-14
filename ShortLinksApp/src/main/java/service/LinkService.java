package service;

import model.Link;

import java.util.List;
import java.util.UUID;

public interface LinkService {

    Link createLink(UUID ownerUuid, String originalUrl, int limit);

    void openLink(UUID ownerUuid,String shortLink);

    List<Link> getUserLinks(UUID ownerUuid);

    Link getLink(String shortLink, UUID currentUserUuid);

    void deleteLink(String shortLink, UUID currentUserUuid);

    void updateLinkLimit(String shortLink, int newLimit, UUID currentUserUuid);
}
