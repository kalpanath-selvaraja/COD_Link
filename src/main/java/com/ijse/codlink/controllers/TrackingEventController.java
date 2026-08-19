package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.TrackingEventDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.TrackingEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "api/tracking-events")
public class TrackingEventController {

    private final TrackingEventService trackingEventService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse createTrackingEvent(@RequestBody TrackingEventDTO trackingEventDTO) {

        return new CommonResponse(0,
                trackingEventService.create(trackingEventDTO),
                "Tracking event recorded");
    }

    @GetMapping(value = "/parcel/{parcelId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getByParcel(@PathVariable Long parcelId) {
        List<TrackingEventDTO> events = trackingEventService.getByParcelId(parcelId);
        return new CommonResponse(0, events, "Tracking events fetched");
    }

}
