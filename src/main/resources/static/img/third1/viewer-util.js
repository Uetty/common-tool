var viewerUtil = {
    currentViewer: null,
    show: function (src, onclose) {
        var image = new Image();
        image.src = src;
        viewer = new Viewer(
            image, {
                title: [1,
                    function (image, imageData) {
                        return imageData.naturalWidth + ' × ' + imageData.naturalHeight;
                    }
                ],
                hidden: function () {
                        viewer.currentViewer = null;
                        viewer.destroy();
                        if (onclose) {
                            onclose(viewer);
                        }
                    },
                    zIndex: 9999,
                navbar: false,
                toolbar: {
                    //oneToOne: {show: true, size: 'medium'},
                    flipHorizontal: {
                        show: true,
                        size: 'medium'
                    },
                    flipVertical: {
                        show: true,
                        size: 'medium'
                    },
                    rotateLeft: {
                        show: true,
                        size: 'medium'
                    },
                    download: function () {
                            var url = viewer.image.src;
                            var alt = viewer.image.alt;

                            //							if (!url.startWidth(location.))
                            //							console.log(url);
                            if (alt.indexOf('.') < 0) {
                                alt += '.jpg';
                            }
                            var $triggerBtn = $('<a id="vie-download-btn" style="display:none;" href="' + url + '" download="' + alt + '" target="_blank">' + alt + '</a>');
                            $('#vie-download-btn').remove();
                            $('body').append($triggerBtn);
                            document.getElementById('vie-download-btn').click();
                            $('#vie-download-btn').remove();
                            //$('#viewer-download-btn').remove();

                        },
                        rotateRight: {
                            show: true,
                            size: 'medium'
                        },
                        zoomIn: {
                            show: true,
                            size: 'medium'
                        },
                        zoomOut: {
                            show: true,
                            size: 'medium'
                        },
                },
                backdrop: false,
            });
        // image.click();
        viewer.show();
        viewerUtil.currentViewer = viewer;
        return viewer;
    },
    hide: function (viewer = null) {
        if (viewer) {
            viewer.hide();
        }
        if (viewerUtil.currentViewer) {
            currentViewer.hide();
        }
    },
    full: function (viewer) {
        if (viewer) {
            viewer.full();
        }
        if (viewerUtil.currentViewer) {
            currentViewer.full();
        }
    }
}