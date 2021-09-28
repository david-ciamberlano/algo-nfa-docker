package it.davidlab.algonfa.controller;

import it.davidlab.algonfa.domain.AssetModel;
import it.davidlab.algonfa.domain.Metadata;
import it.davidlab.algonfa.service.AlgoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;


@RestController
public class MainController {

    AlgoService algoService;

    public MainController(AlgoService algoService) {
        this.algoService = algoService;
    }


    @PostMapping(value = "/asa", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public AssetModel createAsset(@RequestBody AssetModel assetModel) {

        try {
            return algoService.createAsset(assetModel);
        }
        catch (Exception e) {
            //something went wrong...
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Transaction not accepted");
        }
    }


    @GetMapping(value = "/asa/{assetId}/properties",produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Metadata createAsset(@PathVariable long assetId) throws Exception {

        try {
            Optional<Metadata> asaMetadataOpt = algoService.getAssetParams(assetId);
            return asaMetadataOpt.get();
        }
        catch (RuntimeException e) {
            //something went wrong...
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Transaction not accepted");
        }
    }
}
