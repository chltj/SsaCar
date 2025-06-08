package kr.ac.mjc.ssacar.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kr.ac.mjc.ssacar.CardListAdapter;
import kr.ac.mjc.ssacar.PaymentCard;
import kr.ac.mjc.ssacar.R;

public class PaymentLicenseActivity extends AppCompatActivity implements CardListAdapter.OnCardClickListener {
    private static final String TAG = "PaymentLicenseActivity";

    // UI 요소들
    private Button btnAddCard;
    private ImageView ivBack;
    private RecyclerView recyclerViewCards;
    private LinearLayout layoutEmptyState;
    private TextView tvEmptyMessage;

    // 데이터와 어댑터
    private List<PaymentCard> cardList;
    private CardListAdapter cardAdapter;

    // Activity Result Launcher
    private ActivityResultLauncher<Intent> cardRegistrationLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_license);

        Log.d(TAG, "PaymentLicenseActivity 시작");

        // Activity Result Launcher 초기화
        setupActivityResultLauncher();

        // 뷰 초기화
        initViews();

        // 데이터 초기화
        initData();

        // 버튼 설정
        setupButtons();

        // RecyclerView 설정
        setupRecyclerView();

        // 초기 UI 상태 설정
        updateUIState();
    }

    // Activity Result Launcher 설정
    private void setupActivityResultLauncher() {
        cardRegistrationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Log.d(TAG, "카드 등록 결과 수신 - resultCode: " + result.getResultCode());

                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                boolean cardRegistered = data.getBooleanExtra("card_registered", false);
                                Log.d(TAG, "카드 등록 상태: " + cardRegistered);

                                if (cardRegistered) {
                                    String cardType = data.getStringExtra("card_type");
                                    String cardLastFour = data.getStringExtra("card_last_four");
                                    String cardholderName = data.getStringExtra("cardholder_name");
                                    String expiryDate = data.getStringExtra("expiry_date");

                                    Log.d(TAG, "카드 등록 성공: " + cardType + " **** " + cardLastFour);

                                    // 새 카드 추가
                                    addNewCard(cardType, cardLastFour, cardholderName, expiryDate);

                                    Toast.makeText(PaymentLicenseActivity.this,
                                            "카드가 성공적으로 등록되었습니다!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else if (result.getResultCode() == RESULT_CANCELED) {
                            Log.d(TAG, "카드 등록 취소됨");
                        }
                    }
                }
        );

        Log.d(TAG, "Activity Result Launcher 설정 완료");
    }

    private void initViews() {
        try {
            btnAddCard = findViewById(R.id.btn_add_card);
            ivBack = findViewById(R.id.iv_back);
            recyclerViewCards = findViewById(R.id.recycler_view_cards);
            layoutEmptyState = findViewById(R.id.layout_empty_state);
            tvEmptyMessage = findViewById(R.id.tv_empty_message);

            Log.d(TAG, "뷰 초기화 완료");
        } catch (Exception e) {
            Log.e(TAG, "뷰 초기화 실패", e);
            Toast.makeText(this, "화면 초기화 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 테스트용 샘플 카드 추가 (선택사항)
    private void addSampleCard() {
        PaymentCard sampleCard = new PaymentCard("VISA", "4111111111111111", "홍길동", "12/25");
        sampleCard.setDefault(true);
        cardList.add(sampleCard);
    }

    private void setupButtons() {
        try {
            // 뒤로가기 버튼
            if (ivBack != null) {
                ivBack.setOnClickListener(v -> {
                    Log.d(TAG, "뒤로가기 버튼 클릭");
                    finish();
                });
            }

            // 카드 추가 버튼
            if (btnAddCard != null) {
                btnAddCard.setOnClickListener(v -> {
                    Log.d(TAG, "카드 추가 버튼 클릭");
                    goToCardRegistration();
                });
            }

            Log.d(TAG, "버튼 설정 완료");
        } catch (Exception e) {
            Log.e(TAG, "버튼 설정 오류", e);
        }
    }

    private void setupRecyclerView() {
        try {
            if (recyclerViewCards != null) {
                cardAdapter = new CardListAdapter(this, cardList, this);
                recyclerViewCards.setLayoutManager(new LinearLayoutManager(this));
                recyclerViewCards.setAdapter(cardAdapter);

                Log.d(TAG, "RecyclerView 설정 완료");
            }
        } catch (Exception e) {
            Log.e(TAG, "RecyclerView 설정 실패", e);
        }
    }

    // 카드 등록 화면으로 이동
    private void goToCardRegistration() {
        try {
            Log.d(TAG, "카드 등록 화면으로 이동 시도");
            Intent intent = new Intent(this, CardRegistrationActivity.class);
            cardRegistrationLauncher.launch(intent);

        } catch (Exception e) {
            Log.e(TAG, "카드 등록 화면 이동 실패: " + e.getMessage());
            Toast.makeText(this, "카드 등록 화면을 열 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // XML에서 onClick으로 호출되는 메서드 (빈 상태 레이아웃의 버튼용)
    public void goToCardRegistration(View view) {
        Log.d(TAG, "빈 상태 버튼에서 카드 등록 호출");
        goToCardRegistration();
    }

    // 새 카드 추가
    private void addNewCard(String cardType, String cardLastFour, String cardholderName, String expiryDate) {
        try {
            // 전체 카드 번호 재구성 (실제로는 서버에서 받아와야 함)
            String fullCardNumber = "************" + cardLastFour;

            PaymentCard newCard = new PaymentCard(cardType, fullCardNumber, cardholderName, expiryDate);

            // 첫 번째 카드는 기본 카드로 설정
            if (cardList.isEmpty()) {
                newCard.setDefault(true);
            }

            cardList.add(newCard);

            // UI 업데이트
            updateUIState();
            saveCardsToPreferences();

            if (cardAdapter != null) {
                cardAdapter.notifyItemInserted(cardList.size() - 1);
            }

            Log.d(TAG, "새 카드 추가됨: " + newCard.toString());


        } catch (Exception e) {
            Log.e(TAG, "새 카드 추가 실패", e);
        }
    }
    private void saveCardsToPreferences() {
        try {
            ArrayList<String> cardJsonList = new ArrayList<>();
            for (PaymentCard card : cardList) {
                cardJsonList.add(new Gson().toJson(card));
            }

            getSharedPreferences("ssacar", MODE_PRIVATE)
                    .edit()
                    .putStringSet("saved_cards", new HashSet<>(cardJsonList))
                    .apply();

            Log.d(TAG, "카드 정보 SharedPreferences에 저장 완료");

        } catch (Exception e) {
            Log.e(TAG, "카드 저장 실패", e);
        }
    }
    private void initData() {
        try {
            cardList = new ArrayList<>();

            // ✅ SharedPreferences에서 불러오기
            Set<String> savedSet = getSharedPreferences("ssacar", MODE_PRIVATE)
                    .getStringSet("saved_cards", null);

            if (savedSet != null) {
                for (String json : savedSet) {
                    PaymentCard card = new Gson().fromJson(json, PaymentCard.class);
                    cardList.add(card);
                }
                Log.d(TAG, "저장된 카드 불러옴: " + cardList.size() + "개");
            } else {
                Log.d(TAG, "저장된 카드 없음");
            }

        } catch (Exception e) {
            Log.e(TAG, "카드 불러오기 실패", e);
            cardList = new ArrayList<>();
        }
    }



    // UI 상태 업데이트 (빈 상태 vs 카드 목록)
    private void updateUIState() {
        try {
            if (cardList.isEmpty()) {
                // 빈 상태 표시
                if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
                if (recyclerViewCards != null) recyclerViewCards.setVisibility(View.GONE);

                if (tvEmptyMessage != null) {
                    tvEmptyMessage.setText("등록된 카드가 없습니다.\n새 카드를 등록해주세요.");
                }

                Log.d(TAG, "빈 상태 UI 표시");
            } else {
                // 카드 목록 표시
                if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
                if (recyclerViewCards != null) recyclerViewCards.setVisibility(View.VISIBLE);

                Log.d(TAG, "카드 목록 UI 표시 - 카드 수: " + cardList.size());
            }
        } catch (Exception e) {
            Log.e(TAG, "UI 상태 업데이트 실패", e);
        }
    }

    // CardListAdapter.OnCardClickListener 구현
    @Override
    public void onCardClick(PaymentCard card, int position) {
        Log.d(TAG, "카드 클릭: " + card.getCardType() + " " + card.getMaskedCardNumber());
        showCardOptionsDialog(card, position);
    }

    @Override
    public void onCardDelete(PaymentCard card, int position) {
        Log.d(TAG, "카드 삭제 요청: " + card.getCardType() + " " + card.getMaskedCardNumber());
        showDeleteConfirmDialog(card, position);
    }

    @Override
    public void onCardSetDefault(PaymentCard card, int position) {
        Log.d(TAG, "기본 카드 설정: " + card.getCardType() + " " + card.getMaskedCardNumber());
        setDefaultCard(position);
    }

    // 카드 옵션 다이얼로그
    private void showCardOptionsDialog(PaymentCard card, int position) {
        try {
            String[] options;
            if (card.isDefault()) {
                options = new String[]{"카드 삭제"};
            } else {
                options = new String[]{"기본 카드로 설정", "카드 삭제"};
            }

            new AlertDialog.Builder(this)
                    .setTitle(card.getCardType() + " " + card.getMaskedCardNumber())
                    .setItems(options, (dialog, which) -> {
                        if (card.isDefault()) {
                            // 기본 카드인 경우: 삭제만 가능
                            showDeleteConfirmDialog(card, position);
                        } else {
                            // 일반 카드인 경우
                            if (which == 0) {
                                setDefaultCard(position);
                            } else {
                                showDeleteConfirmDialog(card, position);
                            }
                        }
                    })
                    .setNegativeButton("취소", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "카드 옵션 다이얼로그 오류", e);
        }
    }

    // 카드 삭제 확인 다이얼로그
    private void showDeleteConfirmDialog(PaymentCard card, int position) {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("카드 삭제")
                    .setMessage("선택한 카드를 삭제하시겠습니까?\n" +
                            card.getCardType() + " " + card.getMaskedCardNumber())
                    .setPositiveButton("삭제", (dialog, which) -> {
                        deleteCard(position);
                    })
                    .setNegativeButton("취소", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "삭제 확인 다이얼로그 오류", e);
        }
    }

    // 기본 카드 설정
    private void setDefaultCard(int position) {
        try {
            // 모든 카드의 기본 상태 해제
            for (PaymentCard card : cardList) {
                card.setDefault(false);
            }

            // 선택한 카드를 기본 카드로 설정
            cardList.get(position).setDefault(true);

            // UI 업데이트
            if (cardAdapter != null) {
                cardAdapter.notifyDataSetChanged();
            }

            Toast.makeText(this, "기본 카드로 설정되었습니다.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "기본 카드 설정 완료: position " + position);

        } catch (Exception e) {
            Log.e(TAG, "기본 카드 설정 실패", e);
            Toast.makeText(this, "기본 카드 설정에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 카드 삭제
    private void deleteCard(int position) {
        try {
            PaymentCard deletedCard = cardList.get(position);
            boolean wasDefault = deletedCard.isDefault();

            cardList.remove(position);

            // 기본 카드가 삭제된 경우, 첫 번째 카드를 기본 카드로 설정
            if (wasDefault && !cardList.isEmpty()) {
                cardList.get(0).setDefault(true);
            }

            // UI 업데이트
            updateUIState();
            if (cardAdapter != null) {
                cardAdapter.notifyItemRemoved(position);
                if (wasDefault && !cardList.isEmpty()) {
                    cardAdapter.notifyItemChanged(0);
                }
            }

            Toast.makeText(this, "카드가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "카드 삭제 완료: " + deletedCard.toString());

        } catch (Exception e) {
            Log.e(TAG, "카드 삭제 실패", e);
            Toast.makeText(this, "카드 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 카드 목록 새로고침
    private void refreshCardList() {
        try {
            Log.d(TAG, "카드 목록 새로고침");
            // TODO: 실제로는 서버에서 카드 목록을 다시 불러와야 함
            // loadCardsFromServer();

            updateUIState();
            if (cardAdapter != null) {
                cardAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            Log.e(TAG, "카드 목록 새로고침 실패", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "PaymentLicenseActivity 다시 시작");
        refreshCardList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "PaymentLicenseActivity 종료");
    }
}