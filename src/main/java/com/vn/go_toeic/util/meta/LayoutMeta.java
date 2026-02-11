package com.vn.go_toeic.util.meta;

import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LayoutMeta {
    private String title;
    private String activeMenuItem;
    private List<BreadcrumbItem> breadcrumbs;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class BreadcrumbItem {
        private String label;
        private String url;
        private boolean active;

        public BreadcrumbItem(String label, String url) {
            this.label = label;
            this.url = url;
            this.active = (url == null || url.isEmpty());
        }
    }
}
