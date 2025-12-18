package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.CategoryRequest;
import com.spotify.spotify.dto.request.CategoryUpdateRequest;
import com.spotify.spotify.dto.response.CategoryResponse;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.entity.Category;
import com.spotify.spotify.entity.Song;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-17T00:32:08+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Autowired
    private SongMapper songMapper;

    @Override
    public Category toCategory(CategoryRequest request) {
        if ( request == null ) {
            return null;
        }

        Category.CategoryBuilder category = Category.builder();

        category.name( request.getName() );
        category.description( request.getDescription() );

        return category.build();
    }

    @Override
    public CategoryResponse toCategoryResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponse.CategoryResponseBuilder categoryResponse = CategoryResponse.builder();

        categoryResponse.id( category.getId() );
        categoryResponse.name( category.getName() );
        categoryResponse.coverUrl( category.getCoverUrl() );
        categoryResponse.description( category.getDescription() );
        categoryResponse.songs( songSetToSongResponseList( category.getSongs() ) );

        return categoryResponse.build();
    }

    @Override
    public void updateCategory(Category category, CategoryUpdateRequest request) {
        if ( request == null ) {
            return;
        }

        category.setName( request.getName() );
        category.setDescription( request.getDescription() );
        if ( request.getActive() != null ) {
            category.setActive( request.getActive() );
        }
    }

    protected List<SongResponse> songSetToSongResponseList(Set<Song> set) {
        if ( set == null ) {
            return null;
        }

        List<SongResponse> list = new ArrayList<SongResponse>( set.size() );
        for ( Song song : set ) {
            list.add( songMapper.toSongResponse( song ) );
        }

        return list;
    }
}
