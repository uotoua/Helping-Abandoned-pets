package com.catdog.help.repository;

import com.catdog.help.domain.Board.BulletinBoard;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class BulletinBoardRepositoryImpl implements BulletinBoardRepository{

    @PersistenceContext
    EntityManager em;

    public void save(BulletinBoard board) {
        em.persist(board);
    }

    public BulletinBoard findOne(Long no) {
        return em.find(BulletinBoard.class, no);
    }

    public List<BulletinBoard> findAll() {
        List<BulletinBoard> boards = em.createQuery("select b from BulletinBoard b order by board_date desc", BulletinBoard.class).getResultList();
        return boards;
    }
}