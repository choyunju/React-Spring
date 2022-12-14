package com.example.cheezetoon.controller;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.cheezetoon.model.Comment;
import com.example.cheezetoon.model.EpiThumbnail;
import com.example.cheezetoon.model.EpiToon;
import com.example.cheezetoon.model.Episode;
import com.example.cheezetoon.model.Fav;
import com.example.cheezetoon.model.Rate;
import com.example.cheezetoon.model.Toon;
import com.example.cheezetoon.model.ToonThumbnail;
import com.example.cheezetoon.repository.CommentRepository;
import com.example.cheezetoon.repository.EpiThumbnailRepository;
import com.example.cheezetoon.repository.EpiToonRepository;
import com.example.cheezetoon.repository.EpisodeRepository;
import com.example.cheezetoon.repository.FavRepository;
import com.example.cheezetoon.repository.RateRepository;
import com.example.cheezetoon.repository.ToonRepository;
import com.example.cheezetoon.repository.ToonThumbnailRepository;
import com.example.cheezetoon.service.EpiThumbnailService;
import com.example.cheezetoon.service.EpiToonService;
import com.example.cheezetoon.service.ToonThumbnailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RequestMapping("/api")
@RestController
public class ToonController {

    private static final Logger logger = LoggerFactory.getLogger(ToonController.class);

    @Autowired
    private ToonRepository toonRepository;

    @Autowired
    private ToonThumbnailService toonThumbnailService;

    @Autowired
    private EpiThumbnailService epiThumbnailService;
    
    @Autowired
    private EpisodeRepository episodeRepository;

    @Autowired
    private EpiToonService epiToonService;

    @Autowired
    private ToonThumbnailRepository toonThumbnailRepository;

    @Autowired
    private EpiThumbnailRepository epiThumbnailRepository;

    @Autowired
    private EpiToonRepository epiToonRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RateRepository rateRepository;

    @Autowired
    private FavRepository favRepository;
    
    // ??? ?????? ??????
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/newAdd", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public Toon newAdd(@RequestParam("title") String title, @RequestParam("artist") String artist,
            @RequestParam("day") String day, @RequestParam("genre") String genre,
            @RequestParam("file") MultipartFile file) {

        
        
        Toon toon = new Toon(title, artist, day, genre);
        ToonThumbnail toonThumbnail = toonThumbnailService.saveThumbnail(file);
        
        toon.setToonThumbnail(toonThumbnail);

        toonThumbnail.setToon(toon);

        Toon result = toonRepository.save(toon);

        return result;

    }

    // ??? ???????????? ??????
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/newEpi", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public Episode newEpi(@RequestParam("epiTitle") String epiTitle, @RequestParam("toonId") Toon toon, @RequestParam("webtoonId") Integer webtoonId,
            @RequestParam("eFile") MultipartFile eFile, @RequestParam("mFile") MultipartFile mFile) {

        
        
        Episode episode = new Episode(epiTitle, webtoonId, toon);
        EpiThumbnail epiThumbnail = epiThumbnailService.saveEpiThumbnail(eFile);
        EpiToon epiToon = epiToonService.saveEpiToon(mFile);


        episode.setEpiToon(epiToon);
        epiToon.setEpisode(episode);

        episode.setEpiThumbnail(epiThumbnail);
        epiThumbnail.setEpisode(episode);

        Episode result = episodeRepository.save(episode);
        
        return result;

    }

    // ??? ????????? ??????
    @PostMapping("/saveComment/{id}")
    public Episode createComment(@PathVariable int id, @RequestParam("user") String user, @RequestParam("comment") String comment) {
        Episode epi = episodeRepository.findById(id).get();
        Comment com = new Comment(user, comment);
        com.setEpisode(epi);
        
        epi.getComments().add(com);
        return episodeRepository.save(epi);
    }
    
    // ???????????? ??????
    @PostMapping("/saveFav/{id}")
    public Toon saveFav(@PathVariable int id, @RequestParam("user") String user, @RequestParam("title")String title, @RequestParam("webtoonId")Integer webtoonId){
        Toon toon = toonRepository.findById(id).get();
        Fav fav = new Fav(user, title, webtoonId);
        fav.setToon(toon);
        toon.getFav().add(fav);
        return toonRepository.save(toon);

    }

    // webtoonHome ?????? ???????????? ??????
    @DeleteMapping("/deleteFav/{id}/{user}")
    public void deleteFav(@PathVariable("id") int id, @PathVariable("user") String user){
        favRepository.deleteFav(id, user);
    }

    // ???????????? ??????
    @DeleteMapping("/deleteFavById/{id}")
    public void defeFavById(@PathVariable("id") int id){
        favRepository.deleteById(id);
    }

    // ???????????? ????????????
    @GetMapping("/getFav/{user}")
    public Collection<Fav> getFav(@PathVariable("user") String user){
        return favRepository.getFav(user);
    }

    @GetMapping("/getFavById/{tno}/{user}")
    public Collection<Fav> getFav(@PathVariable("tno") Integer tno, @PathVariable("user") String user){
        return favRepository.getFavById(tno, user);
    }

    //????????? ?????? ?????????
    @PutMapping("/uploadEditComment/{id}")
    public Comment uploadEditComment(@PathVariable int id, @RequestParam("comment") String comment){
        Comment com = commentRepository.findById(id).get();
        com.setComment(comment);
        return commentRepository.save(com);
    }

    // Rate ??????
    @PostMapping("/uploadRate/{id}")
    public Episode uploadRate(@PathVariable int id, @RequestParam("user") String user, @RequestParam("rate") Integer rate){
        Episode epi = episodeRepository.findById(id).get();
        Rate r = new Rate(user, rate);
        r.setEpisode(epi);
        epi.getRate().add(r);
        return episodeRepository.save(epi);
    }

    // ?????? Rate ????????????
    @GetMapping(value={"/fetchRate/{id}/{user}"})
    public Optional<Rate> fetchRate(@PathVariable("id") int id, @PathVariable("user") String user){
        return rateRepository.getRateByEpiId(id, user);
    }

    // Rate ??????
    @PutMapping("/uploadEditRate/{id}")
    public Rate uploadEditRate(@PathVariable int id,@RequestParam("user") String user, @RequestParam("rate") Integer rate){
        Rate r = rateRepository.getRateByEpiId(id, user).get();
        r.setRate(rate);
        return rateRepository.save(r);
    }

    // ??? ???????????? ????????? ?????? webtoonId ??? ????????????
    @GetMapping("/getToonIdAndName")
    public List<Map<String, Object>> getTIAN() {
        return toonRepository.getToonIdAndName();
    }

    
    @GetMapping("/getToon")
    public Collection<Toon> getToon() {
        return toonRepository.findAll();
    }

    @GetMapping("/getEpi/{id}")
    public Collection<Episode> getEpi(@PathVariable int id) {
        return episodeRepository.getEpi(id);
    }

    @GetMapping("/getComment/{id}")
    public Collection<Comment> getComment(@PathVariable int id) {
        return commentRepository.getComment(id);
    }
    
    @GetMapping("/getEpiById/{id}")
    public Optional<Episode> getEpiById(@PathVariable int id) {
        return episodeRepository.findById(id);
    }

    @GetMapping("/getToonById/{id}")
    public Optional<Toon> getToonById(@PathVariable int id) {
        return toonRepository.findById(id);
    }


    @GetMapping("/getToonThumbnailById/{id}")
    public Optional<ToonThumbnail> getToonThumbnailById(@PathVariable int id) {
        return toonThumbnailRepository.getToonThumbnailByID(id);
    }

    @DeleteMapping("/deleteToonThumbnail/{id}")
    public void deleteToonThumbnail(@PathVariable Integer id) {
        toonThumbnailRepository.deleteToonThumbnail(id);
    }

    // ?????? ?????? ??????
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteToon/{id}")
    public void deleteToon(@PathVariable Integer id) {
        toonRepository.deleteById(id);
    }

    // ?????? ????????? ??????
    @DeleteMapping("/deleteComment/{id}")
    public void deleteCommen(@PathVariable Integer id){
        commentRepository.deleteById(id);
    }

    //?????? ???????????? ??????
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteEpi/{id}")
    public void deleteEpi(@PathVariable Integer id) {
        episodeRepository.deleteById(id);
    }

    //?????? ???????????? ????????? ?????? ??? ???????????? ????????????
    @GetMapping("/getEditEpi/{id}")
    public Optional<Episode> getEditEpiById(@PathVariable int id){
        return episodeRepository.findById(id);

    }

    //???????????? ?????? ??? ?????? ????????? ????????????
    @GetMapping("/getToonTitle/{id}")
    public Optional<Toon> getToonTitle(@PathVariable int id){
        return toonRepository.findById(id);
    }

    


    // ????????? ?????? ?????????
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/uploadEditToon/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public Toon uploadEditToon(@PathVariable int id, @RequestParam("title") String title, @RequestParam("artist") String artist,
            @RequestParam("day") String day, @RequestParam("genre") String genre,
            @RequestParam("file") MultipartFile file) {
        

        Toon toon = toonRepository.findById(id).get();
        toon.setTitle(title);
        toon.setArtist(artist);
        toon.setDay(day);
        toon.setGenre(genre);

        ToonThumbnail toonThumbnail = toonThumbnailService.saveThumbnail(file);
        
        toon.setToonThumbnail(toonThumbnail);

        toonThumbnail.setToon(toon);

        Toon result = toonRepository.save(toon);

        return result;

    }

    // ????????? ?????? ????????? (?????? ????????? ????????? ???)
    @PutMapping(value = "/uploadEditToonExceptFile/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public Toon uploadEditToonExceptFile(@PathVariable int id, @RequestParam("title") String title, @RequestParam("artist") String artist,
            @RequestParam("day") String day, @RequestParam("genre") String genre) {
        

        Toon toon = toonRepository.findById(id).get();
        toon.setTitle(title);
        toon.setArtist(artist);
        toon.setDay(day);
        toon.setGenre(genre);

        Toon result = toonRepository.save(toon);

        return result;

    }

    // ????????? ???????????? ?????????
    @PutMapping(value = "/uploadEditEpi/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public Episode uploadEditEpi(@PathVariable int id, @RequestParam("epiTitle") String epiTitle,
            @RequestParam("eFile") MultipartFile eFile, @RequestParam("mFile") MultipartFile mFile) {
        

        Episode episode = episodeRepository.findById(id).get();
        episode.setEpiTitle(epiTitle);

        EpiThumbnail epiThumbnail = epiThumbnailService.saveEpiThumbnail(eFile);
        EpiToon epiToon = epiToonService.saveEpiToon(mFile);

        
        
        episode.setEpiToon(epiToon);
        epiToon.setEpisode(episode);

        episode.setEpiThumbnail(epiThumbnail);
        epiThumbnail.setEpisode(episode);

        Episode result = episodeRepository.save(episode);

        return result;

    }

    // ????????? ???????????? ????????? (????????? ???????????? ??????)
    @PutMapping(value = "/uploadEditEpiExceptTaM/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public Episode uploadEditEpiExceptTaM(@PathVariable int id, @RequestParam("epiTitle") String epiTitle) {
        

        Episode episode = episodeRepository.findById(id).get();
        episode.setEpiTitle(epiTitle);

        Episode result = episodeRepository.save(episode);

        return result;

    }

    // ????????? ???????????? ?????????(???????????? ???????????? ??????)
    @PutMapping(value = "/uploadEditEpiExceptM/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public Episode uploadEditEpiExceptM(@PathVariable int id, @RequestParam("epiTitle") String epiTitle,
            @RequestParam("eFile") MultipartFile eFile) {
        

        Episode episode = episodeRepository.findById(id).get();
        episode.setEpiTitle(epiTitle);

        EpiThumbnail epiThumbnail = epiThumbnailService.saveEpiThumbnail(eFile);
        

        episode.setEpiThumbnail(epiThumbnail);
        epiThumbnail.setEpisode(episode);

        Episode result = episodeRepository.save(episode);

        return result;

    }

    // ????????? ???????????? ?????????(????????? ???????????? ??????)
    @PutMapping(value = "/uploadEditEpiExceptT/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public Episode uploadEditEpiExceptT(@PathVariable int id, @RequestParam("epiTitle") String epiTitle,
            @RequestParam("mFile") MultipartFile mFile) {
        

        Episode episode = episodeRepository.findById(id).get();
        episode.setEpiTitle(epiTitle);

        EpiToon epiToon = epiToonService.saveEpiToon(mFile);

        
        
        episode.setEpiToon(epiToon);
        epiToon.setEpisode(episode);


        Episode result = episodeRepository.save(episode);

        return result;

    }

    @GetMapping("/getEpiThumbnailById/{id}")
    public Optional<EpiThumbnail> getEpiThumbnailById(@PathVariable int id) {
        return epiThumbnailRepository.getEpiThumbnailById(id);
    }

    @DeleteMapping("/deleteEpiThumbnail/{id}")
    public void deleteEpiThumbnail(@PathVariable Integer id) {
        epiThumbnailRepository.deleteEpiThumbnail(id);
    }

    @DeleteMapping("/deleteEpiToon/{id}")
    public void deleteEpiToon(@PathVariable Integer id) {
        epiToonRepository.deleteEpiToon(id);
    }

    @GetMapping("/getEpiToon/{id}")
    public Optional<EpiToon> getEpiToon(@PathVariable int id) {
        return epiToonRepository.getEpiToon(id);
    }

    @GetMapping("/getAvgRate/{id}")
    public Double getAvgRate(@PathVariable int id){
        return rateRepository.getAvgRate(id);
    }

    
}
