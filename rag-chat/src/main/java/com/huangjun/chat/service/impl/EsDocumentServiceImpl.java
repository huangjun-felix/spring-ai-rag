package com.huangjun.chat.service.impl;

import com.huangjun.chat.repository.EsRepository;
import com.huangjun.chat.service.EsDocumentService;
import com.huangjun.common.service.BaseService;
import com.huangjun.common.service.DocumentData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EsDocumentServiceImpl implements EsDocumentService, BaseService<DocumentData> {
    private static final Logger logger = LoggerFactory.getLogger(EsDocumentServiceImpl.class);
    private final EsRepository esRepository;

    public EsDocumentServiceImpl(EsRepository esRepository) {
        this.esRepository = esRepository;
    }

    @Override public int insert(DocumentData d) { try { esRepository.save(d); return 1; } catch (Exception e) { logger.error("ES insert failed", e); return 0; } }
    @Override public int updateOne(DocumentData d) { try { esRepository.save(d); return 1; } catch (Exception e) { return 0; } }
    @Override public int deleteOne(DocumentData d) { try { esRepository.delete(d); return 1; } catch (Exception e) { return 0; } }
    @Override public int updateBatch(List<DocumentData> list) { try { esRepository.saveAll(list); return list.size(); } catch (Exception e) { return 0; } }
    @Override public int deleteBatch(List<DocumentData> list) { try { esRepository.deleteAll(list); return list.size(); } catch (Exception e) { return 0; } }
    @Override public List<DocumentData> selectById(int id) { return List.of(); }
    @Override public List<DocumentData> selectAll() { return (List<DocumentData>) esRepository.findAll(); }
}
