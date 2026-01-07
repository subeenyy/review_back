package org.example.platform;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlatformService {

    private final PlatformRepository platformRepository;

    public Platform create(Platform platform){
        return platformRepository.save(platform);
    }

    @Transactional(readOnly = true)
    public List<Platform> findAll(){
        return platformRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Platform findById(Long id){
        return platformRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("플랫폼 정보가 없습니다."));

    }


    public Platform update(Long id, Platform updated){
        Platform platform = findById(id);

        platform = Platform.builder()
                .platformId(platform.getPlatformId())
                .code(updated.getCode())
                .name(updated.getName())
                .rewardEnabled(updated.isRewardEnabled())
                .rewardPolicyId(updated.getRewardPolicyId())
                .active(updated.isActive())
                .build();

        return platformRepository.save(platform);
    }

    public void deactivate(Long id){
        Platform platform = findById(id);
        platformRepository.save(
                Platform.builder()
                        .platformId(platform.getPlatformId())
                        .code(platform.getCode())
                        .name(platform.getName())
                        .rewardEnabled(platform.isRewardEnabled())
                        .rewardPolicyId(platform.getRewardPolicyId())
                        .active(false)
                        .build()
        );
    }
}
