package com.pd.modules.system.service.impl;

import com.pd.modules.system.api.SystemDictService;
import com.pd.modules.system.api.dto.DictDataDTO;
import com.pd.modules.system.api.dto.DictTypeDTO;
import com.pd.modules.system.domain.SysDictData;
import com.pd.modules.system.domain.SysDictType;
import com.pd.modules.system.infrastructure.repository.SysDictDataRepository;
import com.pd.modules.system.infrastructure.repository.SysDictTypeRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SystemDictServiceImpl implements SystemDictService {

    private final SysDictTypeRepository dictTypeRepository;
    private final SysDictDataRepository dictDataRepository;

    public SystemDictServiceImpl(SysDictTypeRepository dictTypeRepository,
                                 SysDictDataRepository dictDataRepository) {
        this.dictTypeRepository = dictTypeRepository;
        this.dictDataRepository = dictDataRepository;
    }

    @Override
    public List<DictTypeDTO> findAllTypes() {
        return dictTypeRepository.findAll().stream()
            .map(this::toTypeDTO)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<DictTypeDTO> findAllActiveTypes() {
        return dictTypeRepository.findByStatus("0").stream()
            .map(this::toTypeDTO)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Optional<DictTypeDTO> findTypeById(Long dictId) {
        return dictTypeRepository.findById(dictId).map(this::toTypeDTO);
    }

    @Override
    public Optional<DictTypeDTO> findTypeByDictType(String dictType) {
        return dictTypeRepository.findByDictType(dictType).map(this::toTypeDTO);
    }

    @Override
    @Transactional
    public DictTypeDTO createType(DictTypeDTO dictTypeDTO) {
        SysDictType dictType = toTypeEntity(dictTypeDTO);
        return toTypeDTO(dictTypeRepository.save(dictType));
    }

    @Override
    @Transactional
    public DictTypeDTO updateType(DictTypeDTO dictTypeDTO) {
        SysDictType dictType = toTypeEntity(dictTypeDTO);
        return toTypeDTO(dictTypeRepository.save(dictType));
    }

    @Override
    @Transactional
    public boolean deleteTypeByIds(Long[] dictIds) {
        for (Long id : dictIds) {
            dictTypeRepository.findById(id).ifPresent(type -> {
                dictDataRepository.deleteByDictType(type.getDictType());
                dictTypeRepository.deleteById(id);
            });
        }
        return true;
    }

    @Override
    public boolean existsByDictType(String dictType) {
        return dictTypeRepository.findByDictType(dictType).isPresent();
    }

    @Override
    public List<DictDataDTO> findDataByType(String dictType) {
        return dictDataRepository.findByDictTypeAndStatusOrderByDictSort(dictType, "0").stream()
            .map(this::toDataDTO)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<DictDataDTO> findDataByTypeOrderBySort(String dictType) {
        return dictDataRepository.findByDictTypeOrderBySort(dictType).stream()
            .map(this::toDataDTO)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<DictDataDTO> findAllData() {
        return dictDataRepository.findAll().stream()
            .map(this::toDataDTO)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Optional<DictDataDTO> findDataById(Long dictCode) {
        return dictDataRepository.findById(dictCode).map(this::toDataDTO);
    }

    @Override
    @Transactional
    public DictDataDTO createData(DictDataDTO dictDataDTO) {
        SysDictData dictData = toDataEntity(dictDataDTO);
        return toDataDTO(dictDataRepository.save(dictData));
    }

    @Override
    @Transactional
    public DictDataDTO updateData(DictDataDTO dictDataDTO) {
        SysDictData dictData = toDataEntity(dictDataDTO);
        return toDataDTO(dictDataRepository.save(dictData));
    }

    @Override
    @Transactional
    public boolean deleteDataByIds(Long[] dictCodes) {
        for (Long id : dictCodes) {
            dictDataRepository.deleteById(id);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean deleteTypeById(Long dictId) {
        dictTypeRepository.findById(dictId).ifPresent(type -> {
            dictDataRepository.deleteByDictType(type.getDictType());
            dictTypeRepository.deleteById(dictId);
        });
        return true;
    }

    @Override
    @Transactional
    public boolean deleteDataById(Long dictCode) {
        dictDataRepository.deleteById(dictCode);
        return true;
    }

    private DictTypeDTO toTypeDTO(SysDictType entity) {
        DictTypeDTO dto = new DictTypeDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private SysDictType toTypeEntity(DictTypeDTO dto) {
        SysDictType entity = new SysDictType();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    private DictDataDTO toDataDTO(SysDictData entity) {
        DictDataDTO dto = new DictDataDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private SysDictData toDataEntity(DictDataDTO dto) {
        SysDictData entity = new SysDictData();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
