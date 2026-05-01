package com.pd.modules.system.service.impl;

import com.pd.modules.system.api.SystemNoticeService;
import com.pd.modules.system.api.dto.NoticeDTO;
import com.pd.modules.system.domain.SysNotice;
import com.pd.modules.system.infrastructure.repository.SysNoticeRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SystemNoticeServiceImpl implements SystemNoticeService {

    private final SysNoticeRepository noticeRepository;

    public SystemNoticeServiceImpl(SysNoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @Override
    public List<NoticeDTO> findAll() {
        return noticeRepository.findAll().stream()
            .map(this::toDTO)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<NoticeDTO> findActiveNotices() {
        return noticeRepository.findByStatus("0").stream()
            .map(this::toDTO)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<NoticeDTO> findByType(String noticeType) {
        return noticeRepository.findByNoticeType(noticeType).stream()
            .map(this::toDTO)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Optional<NoticeDTO> findById(Long noticeId) {
        return noticeRepository.findById(noticeId).map(this::toDTO);
    }

    @Override
    @Transactional
    public NoticeDTO createNotice(NoticeDTO noticeDTO) {
        SysNotice notice = toEntity(noticeDTO);
        return toDTO(noticeRepository.save(notice));
    }

    @Override
    @Transactional
    public NoticeDTO updateNotice(NoticeDTO noticeDTO) {
        SysNotice notice = toEntity(noticeDTO);
        return toDTO(noticeRepository.save(notice));
    }

    @Override
    @Transactional
    public boolean deleteNoticeByIds(Long[] noticeIds) {
        for (Long id : noticeIds) {
            noticeRepository.deleteById(id);
        }
        return true;
    }

    private NoticeDTO toDTO(SysNotice entity) {
        NoticeDTO dto = new NoticeDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private SysNotice toEntity(NoticeDTO dto) {
        SysNotice entity = new SysNotice();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
