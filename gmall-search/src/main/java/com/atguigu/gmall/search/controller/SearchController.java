package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.search.bean.SearchParamVo;
import com.atguigu.gmall.search.bean.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("search")
    public String search(SearchParamVo searchParam, Model model) {

        SearchResponseVo responseVO = this.searchService.search(searchParam);
        model.addAttribute("response", responseVO);
        model.addAttribute("searchParam", searchParam);
        return "search";
    }
}
