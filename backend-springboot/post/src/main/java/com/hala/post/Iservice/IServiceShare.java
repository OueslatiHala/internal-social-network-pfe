package com.hala.post.Iservice;
import com.hala.post.dto.ShareDTO;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface IServiceShare {
    ShareDTO partagerPub(Integer userId, Integer postId);
    void supprimerPartage(Integer shareId);
    List<ShareDTO> getAllShares();
}
