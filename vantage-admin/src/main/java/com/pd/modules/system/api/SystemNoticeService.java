package com.pd.modules.system.api;

import com.pd.modules.system.api.dto.NoticeDTO;
import java.util.List;
import java.util.Optional;

/**
 * System module public API for notice operations.
 */
public interface SystemNoticeService {

    List<NoticeDTO> findAll();

    List<NoticeDTO> findActiveNotices();

    List<NoticeDTO> findByType(String noticeType);

    Optional<NoticeDTO> findById(Long noticeId);

    NoticeDTO createNotice(NoticeDTO notice);

    NoticeDTO updateNotice(NoticeDTO notice);

    boolean deleteNoticeByIds(Long[] noticeIds);
}
