package com.example.http.dto;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResponseLoginDto implements Parcelable {

    private String grantType;
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresIn;
    private Long id;
    private String userId;
    private String name;
    private String role;

    @Builder
    public ResponseLoginDto(String grantType, String accessToken, String refreshToken, Long accessTokenExpiresIn, Long id, String userId, String name, String role) {
        this.grantType = grantType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.role = role;
    }

    protected ResponseLoginDto(Parcel in) {
        grantType = in.readString();
        accessToken = in.readString();
        refreshToken = in.readString();
        accessTokenExpiresIn = in.readLong();
        id = in.readLong();
        userId = in.readString();
        name = in.readString();
        role = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.grantType);
        dest.writeString(this.accessToken);
        dest.writeString(this.refreshToken);
        dest.writeLong(this.accessTokenExpiresIn);
        dest.writeLong(this.id);
        dest.writeString(this.userId);
        dest.writeString(this.name);
        dest.writeString(this.role);
    }

    public static final Creator<ResponseLoginDto> CREATOR = new Creator<ResponseLoginDto>() {
        @Override
        public ResponseLoginDto createFromParcel(Parcel in) {
            return new ResponseLoginDto(in);
        }

        @Override
        public ResponseLoginDto[] newArray(int size) {
            return new ResponseLoginDto[size];
        }
    };
}
