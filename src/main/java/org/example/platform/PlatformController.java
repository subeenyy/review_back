package org.example.platform;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/platforms")
@RequiredArgsConstructor
public class PlatformController {

    private final PlatformService platformService;

    @PostMapping
    public Platform create(@RequestBody Platform platform){
        return platformService.create(platform);
    }

    @GetMapping
    public List<Platform> findAll(){
        return platformService.findAll();
    }

    @GetMapping("/{id}")
    public Platform findOne(@PathVariable Long id){
        return platformService.findById(id);
    }

    @PutMapping("/{id}")
    public Platform update(
            @PathVariable Long id,
            @RequestBody Platform platform
    ){
        return platformService.update(id, platform);

    }

    @DeleteMapping("/{id}")
    public void deactivate(@PathVariable Long id){
        platformService.deactivate(id);
    }
}
