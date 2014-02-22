package com.fusionspy.beacon.site.tux;

import com.fusionspy.beacon.site.SitesHolder;
import com.fusionspy.beacon.site.tux.entity.SiteListEntity;
import com.fusionspy.beacon.system.service.SystemService;
import com.fusionspy.beacon.common.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
class TuxSiteHolder extends SitesHolder<TuxSite>{


    @Autowired
    private SystemService systemService;

    @Resource(name = "tuxDataConnectRepo")
    private TuxDataRepository conRep;

    @Resource(name = "tuxDataSimulationRep")
    private TuxDataSimulationRepository demoRep;

    @Autowired
    private TuxService tuxService;

    @PostConstruct
    void init(){
        for(SiteListEntity siteListEntity: systemService.getSites()){
             super.addMonitorSite(create(siteListEntity));
        }
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.Tuxedo;
    }

    @Override
    public TuxSite createSite(String siteName) {
        SiteListEntity siteListEntity = systemService.getSite(siteName);
        if (siteListEntity != null) {
            return create(siteListEntity);
        } else {
            throw new IllegalStateException("tux查询不到此站点:[" + siteName + "]，请检查");
        }
    }

    TuxSite create(SiteListEntity siteListEntity){
        TuxSite tuxSite = new TuxSite(siteListEntity);
        tuxSite.setTuxService(tuxService);
        tuxSite.setAttributeCache(this.attributeCache);
        tuxSite.setResourcesCache(this.resourcesCache);
        if (demo) {
            tuxSite.setMonitorDataRepository(demoRep);
        } else {
            tuxSite.setMonitorDataRepository(conRep);
        }
        tuxSite.setScheduledExecutorService(executorService);
        return tuxSite;
    }
}
